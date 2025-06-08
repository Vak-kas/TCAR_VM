package com.hanbat.dotcar.monitoring;

import com.hanbat.dotcar.kubernetes.domain.PodStatus;
import com.hanbat.dotcar.monitoring.dto.PodStatusDto;
import com.hanbat.dotcar.monitoring.service.PodMonitoringService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/monitor")
@RequiredArgsConstructor
public class PodMonitoringController {
    private final PodMonitoringService podMonitoringService;

    @GetMapping("/pod")
    public ResponseEntity<?> getPodStatus(@RequestParam String podName, @RequestParam String podNamespace) {
        try {
            PodStatusDto podStatus = podMonitoringService.getPodStatus(podName, podNamespace);
            return ResponseEntity.status(HttpStatus.OK).body(podStatus);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("에러 발생: " + e.getMessage());
        }
    }
}
