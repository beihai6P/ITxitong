package com.company.itoms.controller;

import com.company.itoms.common.SPI;
import com.company.itoms.dto.request.WorkOrderCreateDTO;
import com.company.itoms.dto.response.AiRecommendationDTO;
import com.company.itoms.service.AiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/ai")
public class AiController {

    @Autowired
    private ApplicationContext applicationContext;

    private AiService aiService;

    @PostConstruct
    public void init() {
        Map<String, AiService> beans = applicationContext.getBeansOfType(AiService.class);
        for (AiService service : beans.values()) {
            SPI spi = AnnotationUtils.findAnnotation(service.getClass(), SPI.class);
            if (spi != null && "aliyun_ai".equals(spi.value())) {
                this.aiService = service;
                return;
            }
        }
        // Fallback to Baidu if Aliyun is not found but it shouldn't happen based on requirements
        if (this.aiService == null && !beans.isEmpty()) {
            this.aiService = beans.values().iterator().next();
        }
    }

    @PostMapping("/recommend")
    public ResponseEntity<?> getRecommendation(@RequestBody WorkOrderCreateDTO request) {
        AiRecommendationDTO recommendation = aiService.recommend(request);

        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "success");
        response.put("data", recommendation);
        return ResponseEntity.ok(response);
    }
}
