package com.interview.service;

import com.interview.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * PDF 解析服务 - 从 PDF 文件中提取纯文本
 *
 * @Author Diamond
 * @Create 2026/6/5
 */
@Slf4j
@Service
public class PdfParseService {

    /**
     * 从 OSS URL 下载 PDF 并提取文本
     *
     * @param fileUrl PDF 文件的 OSS 访问地址
     * @return 提取的纯文本内容
     */
    public String extractTextFromUrl(String fileUrl) {
        log.debug("开始解析 PDF: {}", fileUrl);
        try (InputStream inputStream = new URL(fileUrl).openStream();
             PDDocument document = Loader.loadPDF(inputStream.readAllBytes())) {

            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document).trim();

            if (text.isEmpty()) {
                throw new BusinessException("PDF 文件内容为空，可能是扫描件，暂不支持");
            }

            log.debug("PDF 解析完成，提取文本长度: {} 字符", text.length());
            return text;
        } catch (BusinessException e) {
            throw e;
        } catch (IOException e) {
            log.error("PDF 解析失败: {}", fileUrl, e);
            throw new BusinessException("PDF 文件解析失败，请确认文件格式正确");
        }
    }

}
