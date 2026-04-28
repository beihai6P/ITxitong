package com.company.itoms.service.impl;

import com.company.itoms.dto.AiRecommendationDTO;
import com.company.itoms.dto.request.WorkOrderCreateDTO;
import com.company.itoms.entity.AssetEntity;
import com.company.itoms.entity.WorkOrderEntity;
import com.company.itoms.exception.AiApiException;
import com.company.itoms.mapper.AssetMapper;
import com.company.itoms.mapper.WorkOrderFlowLogMapper;
import com.company.itoms.mapper.WorkOrderMapper;
import com.company.itoms.service.AiApiClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
public class WorkOrderServiceImplTest {

    @Mock
    private WorkOrderMapper workOrderMapper;

    @Mock
    private WorkOrderFlowLogMapper workOrderFlowLogMapper;

    @Mock
    private AssetMapper assetMapper;

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
        dto.setCreatorId(1L);

        Mockito.lenient().when(assetMapper.selectById(1L)).thenReturn(createMockAsset());

        UserDetailsServiceImpl.CustomUserDetails user = new UserDetailsServiceImpl.CustomUserDetails(
                "testuser", "password", Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")), 1L);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
        SecurityContextHolder.setContext(context);
    }

    private AssetEntity createMockAsset() {
        AssetEntity asset = new AssetEntity();
        asset.setId(1L);
        asset.setAssetCode("ASSET001");
        asset.setAssetStatus("IN_USE");
        asset.setRepairCount(1);
        asset.setAge(2.0);
        asset.setTotalLife(10.0);
        return asset;
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