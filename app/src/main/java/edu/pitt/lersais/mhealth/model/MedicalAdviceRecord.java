package edu.pitt.lersais.mhealth.model;

import java.util.HashMap;
import java.util.Map;

/**
 * The MedicalAdviceRecord entity that represents a medical advice record.
 *
 * @author Haobing Huang and Runhua Xu.
 */

public class MedicalAdviceRecord {
    private String title;
    private String bitmap;  // covert bitmap to string
    private String content;
    private String nonce;

    public MedicalAdviceRecord() {
    }

    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBitmap() {
        return bitmap;
    }

    public void setBitmap(String bitmap) {
        this.bitmap = bitmap;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("title", title);
        result.put("bitmap", bitmap);
        result.put("content", content);
        result.put("nonce", nonce);
        return result;
    }
}
