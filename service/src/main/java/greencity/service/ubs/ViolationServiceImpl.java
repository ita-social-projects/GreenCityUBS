package greencity.service.ubs;

import greencity.constant.OrderHistory;
import greencity.dto.*;
import greencity.entity.enums.SortingOrder;
import greencity.entity.enums.ViolationLevel;
import greencity.entity.order.Order;
import greencity.entity.user.User;
import greencity.entity.user.Violation;
import greencity.exceptions.NotFoundException;
import greencity.exceptions.OrderViolationException;
import greencity.exceptions.UnexistingOrderException;
import greencity.exceptions.UserNotFoundException;
import greencity.repository.OrderRepository;
import greencity.repository.UserRepository;
import greencity.repository.UserViolationsTableRepo;
import greencity.repository.ViolationRepository;
import greencity.service.NotificationServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static greencity.constant.ErrorMessage.*;

@Service
@AllArgsConstructor
public class ViolationServiceImpl implements ViolationService {
    private ViolationRepository violationRepository;
    private UserRepository userRepository;
    private UserViolationsTableRepo userViolationsTableRepo;
    private OrderRepository orderRepository;

    private EventService eventService;
    private NotificationServiceImpl notificationService;
    private FileService fileService;

    @Override
    public UserViolationsWithUserName getAllViolations(Pageable page, Long userId, String columnName,
        SortingOrder sortingOrder) {
        String username = getUsername(userId);
        Long numberOfViolations = violationRepository.getNumberOfViolationsByUser(userId);

        Page<Violation> violationPage = userViolationsTableRepo.findAll(userId, columnName, sortingOrder, page);

        List<UserViolationsDto> userViolationsList =
            violationPage.getContent()
                .stream()
                .map(this::getAllViolations)
                .collect(Collectors.toList());

        return UserViolationsWithUserName.builder()
            .userViolationsDto(new PageableDto<>(userViolationsList, numberOfViolations,
                violationPage.getPageable().getPageNumber(), violationPage.getTotalPages()))
            .fullName(username)
            .build();
    }

    private UserViolationsDto getAllViolations(Violation violation) {
        return UserViolationsDto.builder()
            .violationDate(violation.getViolationDate())
            .orderId(violationRepository.getOrderIdByViolationId(violation.getId()))
            .violationLevel(violation.getViolationLevel())
            .build();
    }

    private String getUsername(Long userID) {
        User currentUser = userRepository.getOne(userID);
        return currentUser.getRecipientName() + " " + currentUser.getRecipientSurname();
    }

    @Override
    public void addUserViolation(AddingViolationsToUserDto add, MultipartFile[] multipartFiles, String uuid) {
        Order order = orderRepository.findById(add.getOrderID()).orElseThrow(() -> new UnexistingOrderException(
            ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST));
        User currentUser = userRepository.findUserByUuid(uuid)
            .orElseThrow(() -> new UserNotFoundException(USER_WITH_CURRENT_ID_DOES_NOT_EXIST));
        if (violationRepository.findByOrderId(order.getId()).isEmpty()) {
            User user = order.getUser();
            Violation violation = violationBuilder(add, order, currentUser);
            if (multipartFiles.length > 0) {
                List<String> images = new LinkedList<>();
                setImages(multipartFiles, images);
                violation.setImages(images);
            }
            violationRepository.save(violation);
            user.setViolations(userRepository.countTotalUsersViolations(user.getId()));
            userRepository.save(user);
            eventService.save(OrderHistory.ADD_VIOLATION, currentUser.getRecipientName()
                + "  " + currentUser.getRecipientSurname(), order);
            notificationService.notifyAddViolation(order);
        } else {
            throw new OrderViolationException(ORDER_ALREADY_HAS_VIOLATION);
        }
    }

    private Violation violationBuilder(AddingViolationsToUserDto add, Order order, User addedByUser) {
        return Violation.builder()
            .violationLevel(ViolationLevel.valueOf(add.getViolationLevel().toUpperCase()))
            .description(add.getViolationDescription())
            .violationDate(order.getOrderDate())
            .order(order)
            .addedByUser(addedByUser)
            .build();
    }

    /**
     * Method returns detailed information about user violation by order id.
     *
     * @param orderId of {@link Long} order id;
     * @return {@link ViolationDetailInfoDto};
     * @author Rusanovscaia Nadejda
     */

    @Override
    @Transactional
    public Optional<ViolationDetailInfoDto> getViolationDetailsByOrderId(Long orderId) {
        Optional<Violation> optionalViolation = violationRepository.findByOrderId(orderId);
        if (optionalViolation.isEmpty()) {
            return Optional.empty();
        }
        Violation violation = optionalViolation.get();
        User addedByUser = userRepository.findById(violation.getAddedByUser().getId())
            .orElseThrow(() -> new NotFoundException(EMPLOYEE_NOT_FOUND));

        return Optional.of(ViolationDetailInfoDto.builder()
            .orderId(orderId)
            .violationLevel(violation.getViolationLevel())
            .description(violation.getDescription())
            .images(violation.getImages())
            .violationDate(violation.getViolationDate())
            .addedByUser(String.join(" ", addedByUser.getRecipientName(), addedByUser.getRecipientSurname()))
            .build());
    }

    @Override
    @Transactional
    public void deleteViolation(Long id, String uuid) {
        User currentUser = userRepository.findUserByUuid(uuid)
            .orElseThrow(() -> new UserNotFoundException(USER_WITH_CURRENT_ID_DOES_NOT_EXIST));
        Optional<Violation> violationOptional = violationRepository.findByOrderId(id);
        if (violationOptional.isPresent()) {
            List<String> images = violationOptional.get().getImages();
            if (!images.isEmpty()) {
                for (int i = 0; i < images.size(); i++) {
                    fileService.delete(images.get(i));
                }
            }
            violationRepository.deleteById(violationOptional.get().getId());
            User user = violationOptional.get().getOrder().getUser();
            user.setViolations(userRepository.countTotalUsersViolations(user.getId()));
            userRepository.save(user);
            eventService.save(OrderHistory.DELETE_VIOLATION, currentUser.getRecipientName()
                + "  " + currentUser.getRecipientSurname(), violationOptional.get().getOrder());
        } else {
            throw new UnexistingOrderException(VIOLATION_DOES_NOT_EXIST);
        }
    }

    @Override
    public void updateUserViolation(UpdateViolationToUserDto add, MultipartFile[] multipartFiles, String uuid) {
        User currentUser = userRepository.findUserByUuid(uuid)
            .orElseThrow(() -> new UserNotFoundException(USER_WITH_CURRENT_ID_DOES_NOT_EXIST));
        Violation violation = violationRepository.findByOrderId(add.getOrderID())
            .orElseThrow(() -> new UnexistingOrderException(ORDER_HAS_NOT_VIOLATION));
        updateViolation(violation, add, multipartFiles);
        violationRepository.save(violation);
        eventService.save(OrderHistory.CHANGES_VIOLATION,
            currentUser.getRecipientName() + "  " + currentUser.getRecipientSurname(), violation.getOrder());
    }

    private void updateViolation(Violation violation, UpdateViolationToUserDto add, MultipartFile[] multipartFiles) {
        violation.setViolationLevel(ViolationLevel.valueOf(add.getViolationLevel().toUpperCase()));
        violation.setDescription(add.getViolationDescription());
        List<String> violationImages = violation.getImages();
        if (add.getImagesToDelete() != null) {
            List<String> images = add.getImagesToDelete();
            for (String image : images) {
                fileService.delete(image);
                violationImages.remove(image);
            }
        }
        if (multipartFiles.length > 0) {
            List<String> images = new LinkedList<>();
            setImages(multipartFiles, images);
            if (violation.getImages().isEmpty()) {
                violation.setImages(images);
            } else {
                violation
                    .setImages(Stream.concat(violationImages.stream(), images.stream()).collect(Collectors.toList()));
            }
        }
    }

    private void setImages(MultipartFile[] multipartFiles, List<String> images) {
        for (MultipartFile multipartFile : multipartFiles) {
            images.add(fileService.upload(multipartFile));
        }
    }
}
