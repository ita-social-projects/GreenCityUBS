package greencity.mapping;

import greencity.dto.AllFieldsFromTableDto;
import greencity.dto.CertificateDtoForAdding;
import greencity.entity.order.Certificate;
import java.util.Map;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class AllValuesFromTableMapper extends AbstractConverter<Map<String,Object>, AllFieldsFromTableDto> {
    @Override
    protected AllFieldsFromTableDto convert(Map<String, Object> map) {
        return AllFieldsFromTableDto.builder()
            .orderId((Long) map.get("orderid"))
            .orderStatus("++++++++++++++++++")
            .orderDate(map.get("order_date").toString())
            .clientName((String) map.get("clientname"))
            .phoneNumber((String) map.get("phone_number"))
            .email((String) map.get("email"))
            .violationsAmount((Integer) map.get("violations"))
            .district("++++++++++++++++++")
            .address((String) map.get("address"))
            .recipientName((String) map.get("recipient_name"))
            .emailRecipient((String) map.get("recipient_email"))
            .phoneNumberRecipient((String) map.get("recipient_phone"))
            .commentToAddressForClient((String) map.get("comment_to_address_for_client"))
            .garbageBags120Amount((Integer) map.get("garbage_bags_120_amount"))
            .boBags120Amount((Integer) map.get("bo_bags_120_amount"))
            .boBags20Amount((Integer) map.get("bo_bags_20_amount"))
            .totalSumOrder((Long) map.get("total_order_sum"))
            .certificateNumber((String) map.get("code"))
            .discount((Integer) map.get("points"))
            .amountDue((Long) map.get("amount_due"))
            .commentForOrderByClient((String) map.get("comment_for_order_by_client"))
            .payment((String) map.get("payment_system"))
            .dateOfExport(map.get("date_of_export").toString())
            .timeOfExport((String) map.get("time_of_export"))
            .idOrderFromShop((Long) map.get("id_order_from_shop"))
            .receivingStation((String) map.get("receiving_station"))
            .commentsForOrder((String) map.get("comments_for_order"))
            .build();
    }
}
