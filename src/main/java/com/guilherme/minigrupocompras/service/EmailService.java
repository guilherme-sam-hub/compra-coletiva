package com.guilherme.minigrupocompras.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    private static final String REMETENTE = "no-reply@minigrupo.com";

    public void enviarLinkResetSenha(String destinatario, String nomeUsuario, String link) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(REMETENTE);
        msg.setTo(destinatario);
        msg.setSubject("Redefinição de senha - Compra Coletiva");

        String corpo = """
                Olá, %s!

                Recebemos um pedido de redefinição de senha para sua conta na Compra Coletiva.

                Clique no link abaixo para criar uma nova senha (válido por 30 minutos):

                %s

                Se você não solicitou essa alteração, ignore este email.
                Sua senha continua a mesma.

                -- 
                Equipe Compra Coletiva
                """.formatted(nomeUsuario, link);

        msg.setText(corpo);

        try {
            mailSender.send(msg);
            log.info("📧 Email de reset enviado para {}", destinatario);
        } catch (Exception e) {
            log.error("❌ Falha ao enviar email para {}: {}", destinatario, e.getMessage());
            throw e;
        }
    }
}