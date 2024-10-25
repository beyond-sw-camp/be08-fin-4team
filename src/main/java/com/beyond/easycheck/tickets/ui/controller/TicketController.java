package com.beyond.easycheck.tickets.ui.controller;

import com.beyond.easycheck.common.ui.view.ApiResponseView;
import com.beyond.easycheck.tickets.application.service.TicketOperationUseCase;
import com.beyond.easycheck.tickets.application.service.TicketOperationUseCase.TicketCreateCommand;
import com.beyond.easycheck.tickets.application.service.TicketOperationUseCase.TicketUpdateCommand;
import com.beyond.easycheck.tickets.application.service.TicketReadUseCase;
import com.beyond.easycheck.tickets.application.service.TicketReadUseCase.FindTicketResult;
import com.beyond.easycheck.tickets.ui.requestbody.TicketRequest;
import com.beyond.easycheck.tickets.ui.view.TicketView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "Ticket", description = "입장권 정보 관리 API")
@RestController
@RequestMapping("/api/v1/parks")
@RequiredArgsConstructor
public class TicketController {

    private final TicketOperationUseCase ticketOperationUseCase;
    private final TicketReadUseCase ticketReadUseCase;

    @Operation(summary = "입장권 종류를 등록하는 API")
    @PostMapping("/{themeParkId}/tickets")
    public ResponseEntity<ApiResponseView<TicketView>> createTicket(@PathVariable Long themeParkId,
                                                                    @RequestBody @Validated TicketRequest request) {
        TicketCreateCommand command = TicketCreateCommand.builder()
                .themeParkId(themeParkId)
                .ticketName(request.getTicketName())
                .price(request.getPrice())
                .saleStartDate(request.getSaleStartDate())
                .saleEndDate(request.getSaleEndDate())
                .validFromDate(request.getValidFromDate())
                .validToDate(request.getValidToDate())
                .build();

        FindTicketResult result = FindTicketResult.fromEntity(ticketOperationUseCase.createTicket(command));

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponseView<>(new TicketView(result)));
    }

    @Operation(summary = "입장권 종류를 수정하는 API")
    @PutMapping("/{ticketId}")
    public ResponseEntity<ApiResponseView<TicketView>> updateTicket(@PathVariable Long themeParkId,
                                                                    @PathVariable Long ticketId,
                                                                    @RequestBody @Validated TicketRequest request) {

        TicketUpdateCommand command = TicketUpdateCommand.builder()
                .ticketName(request.getTicketName())
                .price(request.getPrice())
                .saleStartDate(request.getSaleStartDate())
                .saleEndDate(request.getSaleEndDate())
                .validFromDate(request.getValidFromDate())
                .validToDate(request.getValidToDate())
                .build();

        FindTicketResult updatedResult = FindTicketResult.fromEntity(ticketOperationUseCase.updateTicket(themeParkId, ticketId, command));

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponseView<>(new TicketView(updatedResult)));
    }

    @Operation(summary = "입장권 종류를 삭제하는 API")
    @DeleteMapping("/{ticketId}")
    public ResponseEntity<Void> deleteTicket(@PathVariable Long themeParkId, @PathVariable Long ticketId) {

        ticketOperationUseCase.deleteTicket(themeParkId, ticketId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Operation(summary = "해당 테마파크 내 입장권 종류를 조회하는 API")
    @GetMapping("/{themeParkId}/tickets")
    public ResponseEntity<ApiResponseView<List<TicketView>>> getAllTicketsByThemePark(@PathVariable Long themeParkId) {
        List<FindTicketResult> results = ticketReadUseCase.getTicketsByThemePark(themeParkId);

        List<TicketView> ticketViews = results.stream()
                .map(TicketView::new)
                .collect(Collectors.toList());

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponseView<>(ticketViews));
    }

    @Operation(summary = "해당 테마파크 내 현재시점에서 판매하고 있는 입장권 조회 API")
    @GetMapping("/{themeParkId}/tickets/on-sale")
    public ResponseEntity<ApiResponseView<List<TicketView>>> getTicketsByThemeParkOnSale(@PathVariable Long themeParkId) {
        List<FindTicketResult> results = ticketReadUseCase.getTicketsByThemeParkOnSale(themeParkId);

        List<TicketView> ticketViews = results.stream()
                .map(TicketView::new)
                .collect(Collectors.toList());

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponseView<>(ticketViews));
    }

    @Operation(summary = "입장권 종류를 조회하는 API")
    @GetMapping("/{ticketId}")
    public ResponseEntity<ApiResponseView<TicketView>> getTicketById(@PathVariable Long ticketId) {
        FindTicketResult result = ticketReadUseCase.getTicketById(ticketId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponseView<>(new TicketView(result)));
    }
}
