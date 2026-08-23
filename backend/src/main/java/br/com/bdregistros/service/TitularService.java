package br.com.bdregistros.service;

import br.com.bdregistros.dto.EnderecoDTO;
import br.com.bdregistros.dto.TitularCadastroRequest;
import br.com.bdregistros.exception.ConsentimentoObrigatorioException;
import br.com.bdregistros.exception.CpfInvalidoException;
import br.com.bdregistros.exception.TitularJaCadastradoException;
import br.com.bdregistros.exception.TitularNaoEncontradoException;
import br.com.bdregistros.model.Consentimento;
import br.com.bdregistros.model.Endereco;
import br.com.bdregistros.model.StatusConsentimento;
import br.com.bdregistros.model.StatusTitular;
import br.com.bdregistros.model.Titular;
import br.com.bdregistros.repository.ConsentimentoRepository;
import br.com.bdregistros.repository.LogAcessoRepository;
import br.com.bdregistros.repository.TitularRepository;
import br.com.bdregistros.util.CpfValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class TitularService {

    private final TitularRepository titularRepository;
    private final ConsentimentoRepository consentimentoRepository;
    private final LogAcessoRepository logAcessoRepository;

    public TitularService(TitularRepository titularRepository,
                           ConsentimentoRepository consentimentoRepository,
                           LogAcessoRepository logAcessoRepository) {
        this.titularRepository = titularRepository;
        this.consentimentoRepository = consentimentoRepository;
        this.logAcessoRepository = logAcessoRepository;
    }

    @Transactional
    public Titular cadastrar(TitularCadastroRequest request) {
        String cpf = CpfValidator.somenteDigitos(request.getCpf());
        if (!CpfValidator.isValid(cpf)) {
            throw new CpfInvalidoException("CPF invalido.");
        }
        if (!request.isConsentimentoConfirmado()) {
            throw new ConsentimentoObrigatorioException("O consentimento e obrigatorio para o cadastro.");
        }
        if (titularRepository.existsByCpf(cpf)) {
            throw new TitularJaCadastradoException("Ja existe cadastro para este CPF.");
        }

        Titular titular = new Titular();
        titular.setNomeCompleto(request.getNomeCompleto().trim());
        titular.setCpf(cpf);
        titular.setTituloEleitor(request.getTituloEleitor());
        titular.setTelefone(request.getTelefone());
        titular.setDataNascimento(request.getDataNascimento());
        titular.setEndereco(mapEndereco(request.getEndereco(), titular));

        titular = titularRepository.save(titular);

        Consentimento consentimento = new Consentimento();
        consentimento.setTitular(titular);
        consentimento.setFinalidade(request.getFinalidadeConsentimento());
        consentimento.setCanal(request.getCanalConsentimento());
        consentimento.setAgenteResponsavel(request.getAgenteResponsavel());
        consentimento.setDataConsentimento(LocalDateTime.now());
        consentimentoRepository.save(consentimento);

        return titular;
    }

    @Transactional(readOnly = true)
    public Titular buscarPorCpf(String cpf, String usuarioLogado) {
        String digits = CpfValidator.somenteDigitos(cpf);
        Titular titular = titularRepository.findByCpfAndStatusNot(digits, StatusTitular.EXCLUIDO)
                .orElseThrow(() -> new TitularNaoEncontradoException("Nenhum registro encontrado para este CPF."));

        registrarAcesso(titular.getId(), usuarioLogado, "CONSULTA");
        return titular;
    }

    @Transactional
    public void revogarConsentimento(UUID titularId, String usuarioLogado) {
        List<Consentimento> ativos = consentimentoRepository.findByTitularId(titularId).stream()
                .filter(c -> c.getStatus() == StatusConsentimento.ATIVO)
                .toList();

        LocalDateTime agora = LocalDateTime.now();
        ativos.forEach(c -> {
            c.setStatus(StatusConsentimento.REVOGADO);
            c.setDataRevogacao(agora);
        });

        Titular titular = titularRepository.findById(titularId)
                .orElseThrow(() -> new TitularNaoEncontradoException("Titular nao encontrado."));
        titular.setStatus(StatusTitular.INATIVO);

        registrarAcesso(titularId, usuarioLogado, "REVOGACAO_CONSENTIMENTO");
    }

    private void registrarAcesso(UUID titularId, String usuario, String acao) {
        var log = new br.com.bdregistros.model.LogAcesso();
        log.setTitularId(titularId);
        log.setUsuario(usuario);
        log.setAcao(acao);
        logAcessoRepository.save(log);
    }

    private Endereco mapEndereco(EnderecoDTO dto, Titular titular) {
        Endereco endereco = new Endereco();
        endereco.setTitular(titular);
        endereco.setLogradouro(dto.getLogradouro());
        endereco.setNumero(dto.getNumero());
        endereco.setComplemento(dto.getComplemento());
        endereco.setBairro(dto.getBairro());
        endereco.setCidade(dto.getCidade());
        endereco.setEstado(dto.getEstado().toUpperCase());
        endereco.setCep(dto.getCep());
        return endereco;
    }
}
