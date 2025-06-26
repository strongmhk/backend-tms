package com.wrkr.tickety.domains.member.domain.model;

import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class EmailBulkNotificationEvent {

    private final List<String> emails;
}
