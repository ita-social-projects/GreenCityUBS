package greencity.ubstelegrambot.service;

import greencity.entity.telegram.AuthorizedUser;
import greencity.entity.telegram.TelegramManager;
import greencity.entity.telegram.UnknownTelegramUser;
import greencity.entity.user.User;
import greencity.entity.user.employee.Employee;
import greencity.entity.user.employee.Position;
import greencity.enums.TelegramUser;
import greencity.exceptions.NotFoundException;
import greencity.repository.EmployeeRepository;
import greencity.repository.PositionRepository;
import greencity.repository.AuthorizedUserRepository;
import greencity.repository.TelegramManagerRepository;
import greencity.repository.UnknownTelegramUserRepository;
import greencity.repository.UserRepository;
import greencity.service.ubs.TelegramAuthorizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.Message;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TelegramAuthorizationServiceImpl implements TelegramAuthorizationService {
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final PositionRepository positionRepository;
    private final TelegramManagerRepository telegramManagerRepository;
    private final UnknownTelegramUserRepository unknownTelegramUserRepository;
    private final AuthorizedUserRepository telegramBotRepository;
    private static final String POSITION_NOT_FOUND = "Position with id %s not found";

    @Override
    public TelegramUser handleAuthorizedUser(String uuId, String tgUserId) {
        Optional<User> user = userRepository.findUserByUuid(uuId);

        Optional<Employee> employee = employeeRepository.findByUuid(uuId);
        if (employee.isPresent()) {
            var employeePositions = employee.get().getEmployeePosition();
            Position serviceManager = positionRepository.findById(1L)
                .orElseThrow(() -> new NotFoundException(String.format(POSITION_NOT_FOUND, 1)));
            Position manager = positionRepository.findById(2L)
                .orElseThrow(() -> new NotFoundException(String.format(POSITION_NOT_FOUND, 2)));
            if (employeePositions.contains(manager) || employeePositions.contains(serviceManager)) {
                TelegramManager telegramManager = TelegramManager
                    .builder()
                    .chatId(tgUserId)
                    .build();
                telegramManagerRepository.save(telegramManager);
                return TelegramUser.MANAGER;
            }
        } else {
            Optional<UnknownTelegramUser> unknownSavedTelegramUser =
                unknownTelegramUserRepository.findByChatId(tgUserId);
            if (unknownSavedTelegramUser.isPresent()) {
                if (user.isPresent()) {
                    telegramBotRepository.save(createTelegramBotEntity(user.get(), tgUserId, false));
                    unknownTelegramUserRepository.delete(unknownSavedTelegramUser.get());
                }
                return TelegramUser.UNKNOWN_USER;
            }

            Optional<AuthorizedUser> registeredUserBot = telegramBotRepository.findByChatId(tgUserId);
            if (registeredUserBot.isEmpty()) {
                user.ifPresent(
                    value -> telegramBotRepository.save(createTelegramBotEntity(user.get(), tgUserId, false)));
            }
        }
        return TelegramUser.USER;
    }

    @Override
    @Transactional
    public void handleUnknownTelegramUser(Message message) {
        final String tgUserId = String.valueOf(message.getFrom().getId());
        Optional<AuthorizedUser> registeredUserBot = telegramBotRepository.findByChatId(tgUserId);
        UnknownTelegramUser unknownTelegramUser = populateUnknownTelegramUser(message);

        if (registeredUserBot.isEmpty()) {
            unknownTelegramUserRepository.save(unknownTelegramUser);
        }
    }

    private UnknownTelegramUser populateUnknownTelegramUser(Message message) {
        return new UnknownTelegramUser(
            message.getFrom().getId().toString(),
            false,
            message.getFrom().getFirstName(),
            message.getFrom().getLastName() == null ? "" : message.getFrom().getLastName(),
            message.getFrom().getUserName() == null ? "" : message.getFrom().getUserName(),
            message.getContact() != null && message.getContact().getPhoneNumber() != null
                ? message.getContact().getPhoneNumber()
                : "");
    }

    private AuthorizedUser createTelegramBotEntity(User user, String chatId, boolean isManager) {
        return new AuthorizedUser(
            chatId,
            false,
            true,
            user,
            isManager);
    }
}
