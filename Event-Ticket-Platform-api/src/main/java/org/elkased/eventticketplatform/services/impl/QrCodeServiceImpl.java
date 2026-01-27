package org.elkased.eventticketplatform.services.impl;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.RequiredArgsConstructor;
import org.elkased.eventticketplatform.domain.entities.QrCode;
import org.elkased.eventticketplatform.domain.entities.QrCodeStatusEnum;
import org.elkased.eventticketplatform.domain.entities.Ticket;
import org.elkased.eventticketplatform.exceptions.QrCodeGenerationException;
import org.elkased.eventticketplatform.repositories.QrCodeRepository;
import org.elkased.eventticketplatform.services.QrCodeService;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QrCodeServiceImpl implements QrCodeService {
    private static final int QR_HEIGHT = 300;
    private static final int QR_WIDTH = 300;
    private final QrCodeRepository qrCodeRepository;
    private final QRCodeWriter qrCodeWriter;

    @Override
    public QrCode generateQrCode(Ticket ticket) {
        try {
            UUID id = UUID.randomUUID();
            String qrCodeImage = generateQrCodeImage(id);

            QrCode qrCode = QrCode.builder()
                    .id(id)
                    .status(QrCodeStatusEnum.ACTIVE)
                    .value(qrCodeImage)
                    .ticket(ticket)
                    .build();

            return qrCodeRepository.saveAndFlush(qrCode);
        } catch (WriterException ex) {
            throw new QrCodeGenerationException("Failed to generate QR Code", ex);
        }
    }

    private String generateQrCodeImage(UUID uniqueId) throws WriterException {
        BitMatrix bitMatrix = qrCodeWriter.encode(uniqueId.toString(), BarcodeFormat.QR_CODE, QR_WIDTH, QR_HEIGHT);
        BufferedImage qrCodeImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(qrCodeImage, "PNG", baos);
            byte[] byteArray = baos.toByteArray();
            return Base64.getEncoder().encodeToString(byteArray);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
