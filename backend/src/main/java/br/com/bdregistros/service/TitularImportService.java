package br.com.bdregistros.service;

import br.com.bdregistros.dto.ImportErroResponse;
import br.com.bdregistros.dto.ImportResultadoResponse;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackInputStream;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Le o arquivo de restauracao (CSV ou XLSX, mesmo layout gerado por
 * TitularExportService) e delega o upsert de cada linha, em transacoes
 * independentes, para TitularImportRowService.
 */
@Service
public class TitularImportService {

    private final TitularImportRowService rowService;

    public TitularImportService(TitularImportRowService rowService) {
        this.rowService = rowService;
    }

    public ImportResultadoResponse importar(MultipartFile file, String formatoParam, String usuarioLogado, boolean dryRun) {
        String formato = resolverFormato(file, formatoParam);
        List<LinhaImportacao> linhas = "csv".equals(formato) ? lerCsv(file) : lerXlsx(file);

        int criados = 0;
        int atualizados = 0;
        List<ImportErroResponse> erros = new ArrayList<>();

        for (LinhaImportacao linha : linhas) {
            try {
                TitularImportRowService.Resultado resultado = rowService.processarLinha(linha.valores(), usuarioLogado, dryRun);
                if (resultado == TitularImportRowService.Resultado.CRIADO) {
                    criados++;
                } else {
                    atualizados++;
                }
            } catch (Exception e) {
                erros.add(new ImportErroResponse(linha.numero(), e.getMessage()));
            }
        }

        return new ImportResultadoResponse(linhas.size(), criados, atualizados, erros, dryRun);
    }

    private String resolverFormato(MultipartFile file, String formatoParam) {
        if (formatoParam != null && !formatoParam.isBlank()) {
            String normalizado = formatoParam.trim().toLowerCase();
            if (normalizado.equals("csv") || normalizado.equals("xlsx")) {
                return normalizado;
            }
            throw new IllegalArgumentException("Formato invalido: " + formatoParam + " (use csv ou xlsx).");
        }
        String nome = file.getOriginalFilename();
        if (nome != null) {
            String lower = nome.toLowerCase();
            if (lower.endsWith(".csv")) {
                return "csv";
            }
            if (lower.endsWith(".xlsx")) {
                return "xlsx";
            }
        }
        throw new IllegalArgumentException("Nao foi possivel determinar o formato do arquivo; envie .csv ou .xlsx.");
    }

    private List<LinhaImportacao> lerCsv(MultipartFile file) {
        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setIgnoreHeaderCase(true)
                .setTrim(true)
                .build();

        try (InputStream in = semBom(file.getInputStream());
             Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8);
             CSVParser parser = new CSVParser(reader, format)) {
            List<LinhaImportacao> linhas = new ArrayList<>();
            int numeroLinha = 2;
            for (CSVRecord record : parser) {
                linhas.add(new LinhaImportacao(numeroLinha++, record.toMap()));
            }
            return linhas;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private List<LinhaImportacao> lerXlsx(MultipartFile file) {
        try (InputStream in = file.getInputStream(); Workbook workbook = WorkbookFactory.create(in)) {
            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                throw new IllegalArgumentException("Planilha vazia: nenhum cabecalho encontrado.");
            }

            DataFormatter formatter = new DataFormatter();
            Map<Integer, String> colunas = new LinkedHashMap<>();
            for (Cell cell : headerRow) {
                colunas.put(cell.getColumnIndex(), formatter.formatCellValue(cell).trim());
            }

            List<LinhaImportacao> linhas = new ArrayList<>();
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || linhaVazia(row, formatter)) {
                    continue;
                }
                Map<String, String> valores = new LinkedHashMap<>();
                for (Map.Entry<Integer, String> coluna : colunas.entrySet()) {
                    Cell cell = row.getCell(coluna.getKey());
                    valores.put(coluna.getValue(), cell == null ? "" : formatter.formatCellValue(cell).trim());
                }
                linhas.add(new LinhaImportacao(i + 1, valores));
            }
            return linhas;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private boolean linhaVazia(Row row, DataFormatter formatter) {
        for (Cell cell : row) {
            if (!formatter.formatCellValue(cell).trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private InputStream semBom(InputStream in) throws IOException {
        PushbackInputStream pushback = new PushbackInputStream(in, 3);
        byte[] possivelBom = new byte[3];
        int lidos = pushback.read(possivelBom, 0, 3);
        boolean temBom = lidos == 3
                && (possivelBom[0] & 0xFF) == 0xEF
                && (possivelBom[1] & 0xFF) == 0xBB
                && (possivelBom[2] & 0xFF) == 0xBF;
        if (!temBom && lidos > 0) {
            pushback.unread(possivelBom, 0, lidos);
        }
        return pushback;
    }

    private record LinhaImportacao(int numero, Map<String, String> valores) {
    }
}
