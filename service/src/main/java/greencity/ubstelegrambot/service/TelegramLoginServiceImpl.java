package greencity.ubstelegrambot.service;

import greencity.client.UserRemoteClient;
import greencity.constant.TelegramBotConstants;
import greencity.dto.TestersSignInRequest;
import greencity.entity.telegram.TelegramManager;
import greencity.entity.user.employee.Employee;
import greencity.repository.EmployeeRepository;
import greencity.repository.TelegramManagerRepository;
import greencity.service.ubs.TelegramLoginService;
import greencity.ubstelegrambot.messages.MessageFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TelegramLoginServiceImpl implements TelegramLoginService {
    private final TelegramManagerRepository telegramManagerRepository;
    private final EmployeeRepository employeeRepository;
    private final TelegramUtils telegramUtils;
    private final UserRemoteClient userRemoteClient;
    @Value("${greencity.sing-in.secret-token}")
    private String secretToken;
    private static final String USERNAME = "username";

    /**
     * {@inheritDoc}
     */
    @Override
    public void logoutManager(String chatId) {
        telegramManagerRepository.findByChatId(chatId)
            .ifPresent(telegramManagerRepository::delete);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SendMessage processInputManagerCredentialsRequest(Message message) {
        String[] parts = message.getText().split(":");

        if (parts.length < 2) {
            return MessageFactory.createFailLoginMessage(message.getChatId().toString(),
                TelegramBotConstants.INCORRECT_LOGIN_FORMAT);
        }

        String login = parts[0];
        String password = parts[1];

        Optional<Employee> employee = employeeRepository.findByEmailWithPositions(login);

        if (employee.isEmpty()) {
            return MessageFactory.createFailLoginMessage(message.getChatId().toString(),
                TelegramBotConstants.USER_IS_NOT_EMPLOYEE);
        }

        boolean isManager = telegramUtils.checkIsEmployeeManager(employee.get());

        if (!isManager) {
            return MessageFactory.createFailLoginMessage(message.getChatId().toString(),
                TelegramBotConstants.EMPLOYEE_IS_NOT_MANAGER);
        }

        var response = userRemoteClient.signIn(new TestersSignInRequest(login, password, secretToken));

        if (!response.getStatusCode().is2xxSuccessful()) {
            return MessageFactory.createFailLoginMessage(message.getChatId().toString(),
                TelegramBotConstants.SOMETHING_WENT_WRONG_PLEASE_TRY_AGAIN);
        }

        var responseBody = response.getBody();
        String name = (responseBody != null && responseBody.name() != null) ? responseBody.name() : USERNAME;

        telegramManagerRepository.save(
            TelegramManager
                .builder()
                .chatId(message.getChatId().toString())
                .employee(employee.get())
                .build());

        return MessageFactory.createSuccessLoginMessage(
            message.getChatId().toString(),
            name);
    }
}