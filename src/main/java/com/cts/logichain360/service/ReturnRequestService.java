package com.cts.logichain360.service;

import com.cts.logichain360.dto.request.ApproveReturnRequest;
import com.cts.logichain360.dto.request.CreateReturnRequestRequest;
import com.cts.logichain360.dto.request.RejectReturnRequest;
import com.cts.logichain360.dto.response.ReturnRequestResponse;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface ReturnRequestService {

    ResponseEntity<ReturnRequestResponse> createReturnRequest(CreateReturnRequestRequest req);

    ResponseEntity<ReturnRequestResponse> approve(Long returnRequestId, ApproveReturnRequest req);

    ResponseEntity<ReturnRequestResponse> reject(Long returnRequestId, RejectReturnRequest req);

    ResponseEntity<ReturnRequestResponse> completeRestock(Long returnRequestId, String photoFilename);

    ResponseEntity<ReturnRequestResponse> getById(Long returnRequestId);

    ResponseEntity<List<ReturnRequestResponse>> getByCustomerId(Long customerId);

    ResponseEntity<List<ReturnRequestResponse>> getPending();
    
    ResponseEntity<List<ReturnRequestResponse>> getByDriverId(Long driverId);
}