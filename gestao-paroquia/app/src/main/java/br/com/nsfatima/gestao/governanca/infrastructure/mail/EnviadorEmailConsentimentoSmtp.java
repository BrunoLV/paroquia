package br.com.nsfatima.gestao.governanca.infrastructure.mail;

import br.com.nsfatima.gestao.governanca.domain.port.EnviadorEmailConsentimento;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.stereotype.Component;

/**
 * Adaptador SMTP real da porta {@link EnviadorEmailConsentimento}, ativo somente com
 * {@code app.mail.enabled=true}. O servidor é configurado pelas propriedades padrão {@code spring.mail.*}
 * e o remetente por {@code app.mail.remetente} (sem valor padrão: ativar o envio sem remetente derruba a
 * subida, em vez de enviar de um endereço improvisado). O envio roda em thread virtual própria, de modo que
 * a latência do SMTP não diferencia, para quem consulta o portal, CPF existente de inexistente. Falhas de transporte são engolidas e registradas sem
 * endereço nem código, pois a resposta do portal precisa ser indistinguível entre CPF existente e inexistente.
 */
@Component
@ConditionalOnProperty(name = "app.mail.enabled", havingValue = "true")
public class EnviadorEmailConsentimentoSmtp implements EnviadorEmailConsentimento {

    private static final Logger LOG = LogManager.getLogger(EnviadorEmailConsentimentoSmtp.class);

    private final JavaMailSender mailSender;
    private final String remetente;
    private final SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor("email-consentimento-");

    public EnviadorEmailConsentimentoSmtp(JavaMailSender mailSender, @Value("${app.mail.remetente}") String remetente) {
        this.mailSender = mailSender;
        this.remetente = remetente;
        this.executor.setVirtualThreads(true);
    }

    @Override
    public void enviarCodigoOtp(String destinatario, String codigo) {
        enviar(destinatario, "Código de verificação - Portal de Consentimento",
                "Seu código de verificação é " + codigo + ".\n"
                        + "Ele vale por 10 minutos e é de uso único. Se você não solicitou, ignore esta mensagem; "
                        + "nenhuma alteração foi feita em seus dados.");
    }

    @Override
    public void enviarComprovante(String destinatario, String tipoEvento, String protocolo) {
        enviar(destinatario, "Comprovante de manifestação de consentimento",
                "Registramos sua manifestação (" + tipoEvento + "). Protocolo: " + protocolo + ".\n"
                        + "Os assentos dos livros sacramentais permanecem preservados por obrigação canônica "
                        + "(Art. 16, I da LGPD e Cân. 535).");
    }

    private void enviar(String destinatario, String assunto, String texto) {
        SimpleMailMessage mensagem = new SimpleMailMessage();
        mensagem.setFrom(remetente);
        mensagem.setTo(destinatario);
        mensagem.setSubject(assunto);
        mensagem.setText(texto);
        executor.execute(() -> {
            try {
                mailSender.send(mensagem);
            } catch (MailException e) {
                LOG.warn("Falha no envio de e-mail do canal de consentimento: {}", e.getClass().getSimpleName());
            }
        });
    }
}
