package com.company.itoms.service.impl;

import com.company.itoms.dto.AiRecommendationDTO;
import com.company.itoms.dto.request.WorkOrderCreateDTO;
import com.company.itoms.entity.WorkOrderEntity;
import com.company.itoms.exception.AiApiException;
import com.company.itoms.mapper.WorkOrderMapper;
import com.company.itoms.service.AiApiClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
public class WorkOrderServiceImplTest {

    @Mock
    private WorkOrderMapper workOrderMapper;

    @Mock
    private AiApiClient aiApiClient;

    @InjectMocks
    private WorkOrderServiceImpl workOrderService;

    private WorkOrderCreateDTO dto;

    @BeforeEach
    public void setup() {
        dto = new WorkOrderCreateDTO();
        dto.setWorkOrderCode("WO2024001");
        dto.setDescription("打印机卡纸");
        dto.setAssetId(1L);
        dto.setUrgencyLevel(1);
    }

    @Test
    public void testCreateWorkOrder() {
        Mockito.when(workOrderMapper.insert(any(WorkOrderEntity.class))).thenAnswer(invocation -> {
            WorkOrderEntity entity = invocation.getArgument(0);
            entity.setId(100L);
            return 1;
        });

        Long id = workOrderService.createWorkOrder(dto);
        assertNotNull(id);
        assertEquals(100L, id);
    }

    @Test
    public void testCreateWorkOrder_WhenAiFallback_ShouldReturnKnowledgeSolution() {
        Mockito.when(aiApiClient.call(any())).thenThrow(new AiApiException("Timeout"));

        AiRecommendationDTO recommendation = workOrderService.getRecommendation(dto);

        assertNotNull(recommendation);
        assertEquals(999L, recommendation.getSolutionId());
        assertEquals("KNOWLEDGE_BASE", recommendation.getSource());
    }
}
