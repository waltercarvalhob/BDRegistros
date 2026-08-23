package br.com.bdregistros.service;

import br.com.bdregistros.model.Consentimento;
import br.com.bdregistros.model.Endereco;
import br.com.bdregistros.model.LogAcesso;
import br.com.bdregistros.model.StatusConsentimento;
import br.com.bdregistros.model.Titular;
import br.com.bdregistros.repository.ConsentimentoRepository;
import br.com.bdregistros.repository.LogAcessoRepository;
import br.com.bdregistros.repository.TitularRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Gera o backup dos titulares (CSV ou XLSX) com o mesmo layout de colunas
 * esperado por TitularImportService, permitindo restaurar o arquivo exportado.
 */
@Service
public class TitularExportService {

    public static final List<String> COLUNAS = List.of(
            "cpf", "nomeCompleto", "tituloEleitor", "telefone", "dataNascimento", "status",
            "logradouro", "numero", "complemento", "bairro", "cidade", "estado", "cep",
            "finalidadeConsentimento", "canalConsentimento", "agenteResponsavel", "dataConsentimento", "statusConsentimento");

    private static final byte[] UTF8_BOM = new byte[] {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};

    private final TitularRepository titularRepository;
    private final ConsentimentoRepository consentimentoRepository;
    private final LogAcessoRepository logAcessoRepository;

    public TitularExportService(TitularRepository titularRepository,
                                 ConsentimentoRepository consentimentoRepository,
                                 LogAcessoRepository logAcessoRepository) {
        this.titularRepository = titularRepository;
        this.consentimentoRepository = consentimentoRepository;
        this.logAcessoRepository = logAcessoRepository;
    }

    @Transactional
    public byte[] exportarCsv(String usuarioLogado) {
        List<Titular> titulares = titularRepository.findAll();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.writeBytes(UTF8_BOM);
        try (OutputStreamWriter writer = new OutputStreamWriter(baos, StandardCharsets.UTF_8)) {
            CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT.builder().setHeader(COLUNAS.toArray(new String[0])).build());
            for (Titular titular : titulares) {
                printer.printRecord(linha(titular));
            }
            printer.flush();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        registrarExportacao(titulares, usuarioLogado);
        return baos.toByteArray();
    }

    @Transactional
    public byte[] exportarXlsx(String usuarioLogado) {
        List<Titular> titulares = titularRepository.findAll();
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("titulares");
            Row header = sheet.createRow(0);
            for (int i = 0; i < COLUNAS.size(); i++) {
                header.createCell(i).setCellValue(COLUNAS.get(i));
            }

            int rowIndex = 1;
            for (Titular titular : titulares) {
                Row row = sheet.createRow(rowIndex++);
                List<String> valores = linha(titular);
                for (int i = 0; i < valores.size(); i++) {
                    row.createCell(i).setCellValue(valores.get(i));
                }
            }

            workbook.write(baos);
            registrarExportacao(titulares, usuarioLogado);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private List<String> linha(Titular titular) {
        Endereco endereco = titular.getEndereco();
        Consentimento consentimento = consentimentoMaisRelevante(titular.getId());

        return List.of(
                nvl(titular.getCpf()),
                nvl(titular.getNomeCompleto()),
                nvl(titular.getTituloEleitor()),
                nvl(titular.getTelefone()),
                titular.getDataNascimento() == null ? "" : titular.getDataNascimento().toString(),
                titular.getStatus().name(),
                endereco == null ? "" : nvl(endereco.getLogradouro()),
                endereco == null ? "" : nvl(endereco.getNumero()),
                endereco == null ? "" : nvl(endereco.getComplemento()),
                endereco == null ? "" : nvl(endereco.getBairro()),
                endereco == null ? "" : nvl(endereco.getCidade()),
                endereco == null ? "" : nvl(endereco.getEstado()),
                endereco == null ? "" : nvl(endereco.getCep()),
                consentimento == null ? "" : nvl(consentimento.getFinalidade()),
                consentimento == null ? "" : consentimento.getCanal().name(),
                consentimento == null ? "" : nvl(consentimento.getAgenteResponsavel()),
                consentimento == null ? "" : consentimento.getDataConsentimento().toString(),
                consentimento == null ? "" : consentimento.getStatus().name());
    }

    private Consentimento consentimentoMaisRelevante(UUID titularId) {
        List<Consentimento> consentimentos = consentimentoRepository.findByTitularId(titularId);
        return consentimentos.stream()
                .sorted(Comparator.comparing((Consentimento c) -> c.getStatus() == StatusConsentimento.ATIVO ? 0 : 1)
                        .thenComparing(Consentimento::getDataConsentimento, Comparator.reverseOrder()))
                .findFirst()
                .orElse(null);
    }

    private void registrarExportacao(List<Titular> titulares, String usuarioLogado) {
        List<LogAcesso> logs = titulares.stream().map(titular -> {
            LogAcesso log = new LogAcesso();
            log.setTitularId(titular.getId());
            log.setUsuario(usuarioLogado);
            log.setAcao("EXPORTACAO");
            return log;
        }).toList();
        logAcessoRepository.saveAll(logs);
    }

    private String nvl(String valor) {
        return valor == null ? "" : valor;
    }
}
