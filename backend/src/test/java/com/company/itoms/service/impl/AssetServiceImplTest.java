package com.company.itoms.service.impl;

import com.company.itoms.entity.AssetEntity;
import com.company.itoms.mapper.AssetMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class AssetServiceImplTest {

    @Mock
    private AssetMapper assetMapper;

    @InjectMocks
    private AssetServiceImpl assetService;

    private AssetEntity asset;

    @BeforeEach
    public void setup() {
        asset = new AssetEntity();
        asset.setId(1L);
        asset.setAge(2.0);
        asset.setTotalLife(10.0);
        asset.setRepairCount(1);
    }

    @Test
    public void testCalculateHealth() {
        Mockito.when(assetMapper.selectById(1L)).thenReturn(asset);
        
        Double health = assetService.calculateHealth(1L);
        
        // health = (1 - 2/10) * 0.6 + 1 * 0.4 = 0.8 * 0.6 + 0.4 = 0.48 + 0.4 = 0.88
        assertEquals(0.88, health, 0.001);
    }
}
