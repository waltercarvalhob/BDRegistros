package br.com.bdregistros.service;

import br.com.bdregistros.exception.ConsentimentoObrigatorioException;
import br.com.bdregistros.exception.CpfInvalidoException;
import br.com.bdregistros.model.CanalConsentimento;
import br.com.bdregistros.model.Consentimento;
import br.com.bdregistros.model.Endereco;
import br.com.bdregistros.model.LogAcesso;
import br.com.bdregistros.model.StatusConsentimento;
import br.com.bdregistros.model.StatusTitular;
import br.com.bdregistros.model.Titular;
import br.com.bdregistros.repository.ConsentimentoRepository;
import br.com.bdregistros.repository.LogAcessoRepository;
import br.com.bdregistros.repository.TitularRepository;
import br.com.bdregistros.util.CpfValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

/**
 * Processa uma linha do arquivo de restauracao em sua propria transacao
 * (REQUIRES_NEW), para que uma linha invalida nao desfaca as linhas ja
 * processadas com sucesso no mesmo lote. Em modo dryRun (pre-visualizacao),
 * a mesma validacao/upsert roda normalmente, mas a transacao da linha e
 * marcada para rollback no final, entao nada e gravado. O flush() antes do
 * rollback e essencial: sem ele o Hibernate so tentaria de fato o INSERT/UPDATE
 * no banco no commit, entao violacoes de restricao do banco (ex.: tamanho de
 * coluna) so apareceriam na confirmacao real, nunca na pre-visualizacao.
 */
@Service
public class TitularImportRowService {

    public enum Resultado {
        CRIADO,
        ATUALIZADO
    }

    private final TitularRepository titularRepository;
    private final ConsentimentoRepository consentimentoRepository;
    private final LogAcessoRepository logAcessoRepository;

    public TitularImportRowService(TitularRepository titularRepository,
                                    ConsentimentoRepository consentimentoRepository,
                                    LogAcessoRepository logAcessoRepository) {
        this.titularRepository = titularRepository;
        this.consentimentoRepository = consentimentoRepository;
        this.logAcessoRepository = logAcessoRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Resultado processarLinha(Map<String, String> valores, String usuarioLogado, boolean dryRun) {
        String cpf = CpfValidator.somenteDigitos(valor(valores, "cpf"));
        if (!CpfValidator.isValid(cpf)) {
            throw new CpfInvalidoException("CPF invalido: " + valor(valores, "cpf"));
        }

        Optional<Titular> existente = titularRepository.findByCpf(cpf);
        Resultado resultado;
        if (existente.isPresent()) {
            atualizar(existente.get(), valores);
            if (!dryRun) {
                registrarAcesso(existente.get().getId(), usuarioLogado, "IMPORTACAO_ATUALIZACAO");
            }
            resultado = Resultado.ATUALIZADO;
        } else {
            Titular titular = criar(cpf, valores);
            if (!dryRun) {
                registrarAcesso(titular.getId(), usuarioLogado, "IMPORTACAO_CRIACAO");
            }
            resultado = Resultado.CRIADO;
        }

        if (dryRun) {
            titularRepository.flush();
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }
        return resultado;
    }

    private Titular criar(String cpf, Map<String, String> valores) {
        String nomeCompleto = obrigatorio(valores, "nomeCompleto");
        String telefone = obrigatorio(valores, "telefone");
        String logradouro = obrigatorio(valores, "logradouro");
        String bairro = obrigatorio(valores, "bairro");
        String cidade = obrigatorio(valores, "cidade");
        String estado = obrigatorio(valores, "estado");
        if (estado.length() != 2) {
            throw new IllegalArgumentException("estado deve ter 2 letras (UF): " + estado);
        }

        String finalidade = valor(valores, "finalidadeConsentimento");
        String canalRaw = valor(valores, "canalConsentimento");
        if (finalidade.isBlank() || canalRaw.isBlank()) {
            throw new ConsentimentoObrigatorioException(
                    "finalidadeConsentimento e canalConsentimento sao obrigatorios para criar um titular novo.");
        }
        CanalConsentimento canal = parseEnum(CanalConsentimento.class, canalRaw, "canalConsentimento");

        Titular titular = new Titular();
        titular.setNomeCompleto(nomeCompleto);
        titular.setCpf(cpf);
        titular.setTituloEleitor(vazioParaNulo(valor(valores, "tituloEleitor")));
        titular.setTelefone(telefone);
        titular.setDataNascimento(parseData(valor(valores, "dataNascimento")));
        String statusRaw = valor(valores, "status");
        if (!statusRaw.isBlank()) {
            titular.setStatus(parseEnum(StatusTitular.class, statusRaw, "status"));
        }

        Endereco endereco = new Endereco();
        endereco.setTitular(titular);
        endereco.setLogradouro(logradouro);
        endereco.setNumero(vazioParaNulo(valor(valores, "numero")));
        endereco.setComplemento(vazioParaNulo(valor(valores, "complemento")));
        endereco.setBairro(bairro);
        endereco.setCidade(cidade);
        endereco.setEstado(estado.toUpperCase());
        endereco.setCep(vazioParaNulo(valor(valores, "cep")));
        titular.setEndereco(endereco);

        titular = titularRepository.save(titular);

        Consentimento consentimento = new Consentimento();
        consentimento.setTitular(titular);
        consentimento.setFinalidade(finalidade);
        consentimento.setCanal(canal);
        consentimento.setAgenteResponsavel(vazioParaNulo(valor(valores, "agenteResponsavel")));
        consentimento.setDataConsentimento(parseDataHora(valor(valores, "dataConsentimento")).orElseGet(LocalDateTime::now));
        String statusConsentimentoRaw = valor(valores, "statusConsentimento");
        if (!statusConsentimentoRaw.isBlank()) {
            StatusConsentimento status = parseEnum(StatusConsentimento.class, statusConsentimentoRaw, "statusConsentimento");
            consentimento.setStatus(status);
            if (status == StatusConsentimento.REVOGADO) {
                consentimento.setDataRevogacao(LocalDateTime.now());
            }
        }
        consentimentoRepository.save(consentimento);

        return titular;
    }

    private void atualizar(Titular titular, Map<String, String> valores) {
        String nomeCompleto = valor(valores, "nomeCompleto");
        if (!nomeCompleto.isBlank()) {
            titular.setNomeCompleto(nomeCompleto);
        }
        String tituloEleitor = valor(valores, "tituloEleitor");
        if (!tituloEleitor.isBlank()) {
            titular.setTituloEleitor(tituloEleitor);
        }
        String telefone = valor(valores, "telefone");
        if (!telefone.isBlank()) {
            titular.setTelefone(telefone);
        }
        String dataNascimentoRaw = valor(valores, "dataNascimento");
        if (!dataNascimentoRaw.isBlank()) {
            titular.setDataNascimento(parseData(dataNascimentoRaw));
        }
        String statusRaw = valor(valores, "status");
        if (!statusRaw.isBlank()) {
            titular.setStatus(parseEnum(StatusTitular.class, statusRaw, "status"));
        }

        Endereco endereco = titular.getEndereco();
        if (endereco == null) {
            endereco = new Endereco();
            endereco.setTitular(titular);
            titular.setEndereco(endereco);
        }
        atualizarSePresente(valores, "logradouro", endereco::setLogradouro);
        atualizarSePresente(valores, "numero", endereco::setNumero);
        atualizarSePresente(valores, "complemento", endereco::setComplemento);
        atualizarSePresente(valores, "bairro", endereco::setBairro);
        atualizarSePresente(valores, "cidade", endereco::setCidade);
        String estado = valor(valores, "estado");
        if (!estado.isBlank()) {
            if (estado.length() != 2) {
                throw new IllegalArgumentException("estado deve ter 2 letras (UF): " + estado);
            }
            endereco.setEstado(estado.toUpperCase());
        }
        atualizarSePresente(valores, "cep", endereco::setCep);
    }

    private void atualizarSePresente(Map<String, String> valores, String coluna, java.util.function.Consumer<String> setter) {
        String valor = valor(valores, coluna);
        if (!valor.isBlank()) {
            setter.accept(valor);
        }
    }

    private void registrarAcesso(java.util.UUID titularId, String usuario, String acao) {
        LogAcesso log = new LogAcesso();
        log.setTitularId(titularId);
        log.setUsuario(usuario);
        log.setAcao(acao);
        logAcessoRepository.save(log);
    }

    private String obrigatorio(Map<String, String> valores, String coluna) {
        String valor = valor(valores, coluna);
        if (valor.isBlank()) {
            throw new IllegalArgumentException("Coluna obrigatoria ausente para criar titular novo: " + coluna);
        }
        return valor;
    }

    private String valor(Map<String, String> valores, String coluna) {
        String valor = valores.get(coluna);
        return valor == null ? "" : valor.trim();
    }

    private String vazioParaNulo(String valor) {
        return valor.isBlank() ? null : valor;
    }

    private LocalDate parseData(String valor) {
        if (valor.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(valor);
        } catch (Exception e) {
            throw new IllegalArgumentException("dataNascimento invalida (use AAAA-MM-DD): " + valor);
        }
    }

    private Optional<LocalDateTime> parseDataHora(String valor) {
        if (valor.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(LocalDateTime.parse(valor));
        } catch (Exception e) {
            throw new IllegalArgumentException("dataConsentimento invalida (use AAAA-MM-DDTHH:MM:SS): " + valor);
        }
    }

    private <T extends Enum<T>> T parseEnum(Class<T> tipo, String valor, String coluna) {
        try {
            return Enum.valueOf(tipo, valor.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(coluna + " invalido: " + valor);
        }
    }
}
