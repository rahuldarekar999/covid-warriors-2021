package com.covid.warriors.response.model.ocr;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OcrResponse {
    private String status;
    private AnalyzeResult analyzeResult;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public AnalyzeResult getAnalyzeResult() {
        return analyzeResult;
    }

    public void setAnalyzeResult(AnalyzeResult analyzeResult) {
        this.analyzeResult = analyzeResult;
    }
}
