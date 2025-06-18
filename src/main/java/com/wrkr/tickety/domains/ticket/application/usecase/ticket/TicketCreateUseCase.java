package com.wrkr.tickety.domains.ticket.application.usecase.ticket;

import static com.wrkr.tickety.domains.ticket.application.mapper.TicketMapper.mapToTicket;
import static com.wrkr.tickety.domains.ticket.application.mapper.TicketMapper.toTicketPkResponse;
import static com.wrkr.tickety.global.utils.PkCrypto.decrypt;
import static com.wrkr.tickety.global.utils.PkCrypto.encrypt;

import com.wrkr.tickety.domains.member.domain.model.Member;
import com.wrkr.tickety.domains.member.domain.service.MemberGetService;
import com.wrkr.tickety.domains.ticket.application.dto.request.ticket.TicketCreateRequest;
import com.wrkr.tickety.domains.ticket.application.dto.response.TicketPkResponse;
import com.wrkr.tickety.domains.ticket.application.mapper.TicketHistoryMapper;
import com.wrkr.tickety.domains.ticket.domain.constant.ModifiedType;
import com.wrkr.tickety.domains.ticket.domain.constant.TicketStatus;
import com.wrkr.tickety.domains.ticket.domain.model.Category;
import com.wrkr.tickety.domains.ticket.domain.model.Ticket;
import com.wrkr.tickety.domains.ticket.domain.model.TicketHistory;
import com.wrkr.tickety.domains.ticket.domain.service.category.CategoryGetService;
import com.wrkr.tickety.domains.ticket.domain.service.ticket.TicketGetService;
import com.wrkr.tickety.domains.ticket.domain.service.ticket.TicketSaveService;
import com.wrkr.tickety.domains.ticket.domain.service.tickethistory.TicketHistorySaveService;
import com.wrkr.tickety.domains.ticket.exception.TicketErrorCode;
import com.wrkr.tickety.global.annotation.architecture.UseCase;
import com.wrkr.tickety.global.exception.ApplicationException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.transaction.annotation.Transactional;

@UseCase
@RequiredArgsConstructor
@Transactional
public class TicketCreateUseCase {

    private final TicketSaveService ticketSaveService;
    private final CategoryGetService categoryGetService;
    private final MemberGetService UserGetService;
    private final TicketHistorySaveService ticketHistorySaveService;
    private final TicketGetService ticketGetService;
    private final Optional<RedissonClient> redissonClient;

    public TicketPkResponse createTicket(TicketCreateRequest request, Long userId) {
        Category childCategory = categoryGetService.getChildrenCategory(decrypt(request.categoryId()));
        Member member = UserGetService.byMemberId(userId);

        String lockKey = "LOCK:ticket:" + childCategory.getCategoryId();

        if (redissonClient.isEmpty()) {
            // Redis가 없을 경우 락 없이 처리 (주의: 병행성 이슈 있음)
            return createWithoutLock(request, childCategory, member);
        }

        RLock lock = redissonClient.get().getLock(lockKey);

        try {
            if (lock.tryLock(5, 10, TimeUnit.SECONDS)) {
                try {
                    return createInternal(request, childCategory, member);

                } finally {
                    lock.unlock();
                }
            } else {
                throw ApplicationException.from(TicketErrorCode.TICKET_CREATE_LOCK_TIMEOUT);
            }
        } catch (InterruptedException e) {
            throw ApplicationException.from(TicketErrorCode.TICKET_CANNOT_CREATED);
        }
    }

    private String generateSerialNumber(Category childCategory) {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
        String prefix = today + childCategory.getParent().getAbbreviation() + childCategory.getAbbreviation();
        String sequence = ticketGetService.findLastSequence(today, childCategory) == null ? "01" :
            String.format("%02d", Integer.parseInt(ticketGetService.findLastSequence(today, childCategory)) + 1);

        return "#" + prefix + sequence;
    }

    private TicketPkResponse createWithoutLock(TicketCreateRequest request, Category childCategory, Member member) {
        // 락 없이 내부 로직 실행
        return createInternal(request, childCategory, member);
    }

    private TicketPkResponse createInternal(TicketCreateRequest request, Category childCategory, Member member) {
        String serialNumber = generateSerialNumber(childCategory);
        TicketStatus status = TicketStatus.REQUEST;

        Ticket ticket = mapToTicket(request, childCategory, serialNumber, status, member);
        Ticket savedTicket = ticketSaveService.save(ticket);

        ModifiedType modifiedType = ModifiedType.STATUS;
        TicketHistory ticketHistory = TicketHistoryMapper.mapToTicketHistory(savedTicket, modifiedType);
        ticketHistorySaveService.save(ticketHistory);

        return toTicketPkResponse(encrypt(savedTicket.getTicketId()));
    }
}
