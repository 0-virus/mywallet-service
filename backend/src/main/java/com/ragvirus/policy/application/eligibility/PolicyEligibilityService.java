package com.ragvirus.policy.application.eligibility;

import com.ragvirus.policy.domain.Policy;
import com.ragvirus.policy.domain.PolicyCondition;
import com.ragvirus.policy.repository.PolicyConditionRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PolicyEligibilityService {

    private final PolicyConditionRepository conditionRepository;

    public PolicyEligibilityService(PolicyConditionRepository conditionRepository) {
        this.conditionRepository = conditionRepository;
    }

    public EligibilityResult evaluate(Policy policy, UserPolicyCondition userCondition) {
        List<String> matched = new ArrayList<>();
        List<String> needCheck = new ArrayList<>();
        List<String> rejected = new ArrayList<>();

        if ("closed".equals(policy.getApplicationStatus())) {
            rejected.add("신청기간이 마감되었습니다.");
        } else if ("unknown".equals(policy.getApplicationStatus())) {
            needCheck.add("신청기간 확인이 필요합니다.");
        } else {
            matched.add("신청기간 상태가 유효합니다.");
        }

        List<PolicyCondition> conditions = conditionRepository.findByPolicy_Id(policy.getId());
        if (conditions.isEmpty()) {
            needCheck.add("구조화된 신청 조건이 없어 상세 조건 확인이 필요합니다.");
        }

        for (PolicyCondition condition : conditions) {
            evaluateAge(condition, userCondition, matched, needCheck, rejected);
            evaluateTextCondition("고용 상태", condition.getEmploymentStatus(), userCondition.employmentStatus(), matched, needCheck, rejected);
            evaluateTextCondition("소득 조건", condition.getIncomeBand(), userCondition.incomeRange(), matched, needCheck, rejected);
            evaluateTextCondition("가구 조건", condition.getHouseholdStatus(), userCondition.householdStatus(), matched, needCheck, rejected);
            evaluateTextCondition("주거 조건", condition.getHousingStatus(), userCondition.housingStatus(), matched, needCheck, rejected);
            evaluateStudent(condition, userCondition, matched, needCheck, rejected);
            if (condition.isNeedManualCheck()) {
                needCheck.add("정책 조건 문장 해석이 필요합니다.");
            }
        }

        String status;
        if (!rejected.isEmpty()) {
            status = "ineligible";
        } else if (!needCheck.isEmpty()) {
            status = "need_check";
        } else {
            status = "eligible";
        }
        return new EligibilityResult(status, List.copyOf(matched), List.copyOf(needCheck), List.copyOf(rejected));
    }

    private void evaluateAge(
            PolicyCondition condition,
            UserPolicyCondition userCondition,
            List<String> matched,
            List<String> needCheck,
            List<String> rejected
    ) {
        if (condition.getMinAge() == null && condition.getMaxAge() == null) {
            return;
        }
        if (userCondition.age() == null) {
            needCheck.add("나이 정보가 없어 연령 조건 확인이 필요합니다.");
            return;
        }
        if (condition.getMinAge() != null && userCondition.age() < condition.getMinAge()) {
            rejected.add("최소 연령 조건을 충족하지 않습니다.");
            return;
        }
        if (condition.getMaxAge() != null && userCondition.age() > condition.getMaxAge()) {
            rejected.add("최대 연령 조건을 초과했습니다.");
            return;
        }
        matched.add("연령 조건을 충족합니다.");
    }

    private void evaluateStudent(
            PolicyCondition condition,
            UserPolicyCondition userCondition,
            List<String> matched,
            List<String> needCheck,
            List<String> rejected
    ) {
        if (condition.getStudentStatus() == null) {
            return;
        }
        if (userCondition.studentStatus() == null) {
            needCheck.add("학생 여부 확인이 필요합니다.");
            return;
        }
        if (!condition.getStudentStatus().equals(userCondition.studentStatus())) {
            rejected.add("학생 여부 조건을 충족하지 않습니다.");
            return;
        }
        matched.add("학생 여부 조건을 충족합니다.");
    }

    private void evaluateTextCondition(
            String label,
            String required,
            String actual,
            List<String> matched,
            List<String> needCheck,
            List<String> rejected
    ) {
        if (required == null || required.isBlank()) {
            return;
        }
        if (actual == null || actual.isBlank()) {
            needCheck.add(label + " 확인이 필요합니다.");
            return;
        }
        if (required.contains(actual) || actual.contains(required)) {
            matched.add(label + " 조건을 충족합니다.");
            return;
        }
        needCheck.add(label + " 세부 조건 확인이 필요합니다.");
    }
}
