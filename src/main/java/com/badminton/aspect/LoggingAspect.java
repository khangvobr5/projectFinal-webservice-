package com.badminton.aspect;

import com.badminton.dto.BookingDTO;
import com.badminton.dto.UserDTO;
import com.badminton.dto.request.BookingRequest;
import com.badminton.repository.CourtRepository;
import com.badminton.repository.TimeSlotRepository;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    private final CourtRepository courtRepository;
    private final TimeSlotRepository timeSlotRepository;

    public LoggingAspect(CourtRepository courtRepository, TimeSlotRepository timeSlotRepository) {
        this.courtRepository = courtRepository;
        this.timeSlotRepository = timeSlotRepository;
    }

    // Login logging
    @AfterReturning(
            pointcut = "execution(* com.badminton.service.AuthService.login(..))",
            returning = "result"
    )
    public void logLoginSuccess(JoinPoint joinPoint, Object result) {
        Object[] args = joinPoint.getArgs();
        if (args.length > 0) {
            String email = args[0].toString();
            log.info("[AUDIT - LOGIN SUCCESS] User {} logged in successfully", email);
        }
    }

    @AfterThrowing(
            pointcut = "execution(* com.badminton.service.AuthService.login(..))",
            throwing = "ex"
    )
    public void logLoginFailure(JoinPoint joinPoint, Throwable ex) {
        Object[] args = joinPoint.getArgs();
        if (args.length > 0) {
            String email = args[0].toString();
            log.warn("[AUDIT - LOGIN FAILED] User {} failed to login. Error: {}", email, ex.getMessage());
        }
    }

    // Register logging
    @AfterReturning(
            pointcut = "execution(* com.badminton.service.AuthService.register(..))",
            returning = "result"
    )
    public void logRegisterSuccess(JoinPoint joinPoint, Object result) {
        if (result instanceof UserDTO) {
            UserDTO user = (UserDTO) result;
            log.info("[AUDIT - REGISTER SUCCESS] New user registered: {} ({})", user.getUsername(), user.getEmail());
        }
    }

    // Booking logging
    @AfterReturning(
            pointcut = "execution(* com.badminton.service.BookingService.createBooking(..))",
            returning = "result"
    )
    public void logBookingSuccess(JoinPoint joinPoint, Object result) {
        if (result instanceof BookingDTO) {
            BookingDTO booking = (BookingDTO) result;
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String email = (auth != null) ? auth.getName() : "Anonymous";
            String courtName = courtRepository.findById(booking.getCourtId())
                    .map(c -> c.getName())
                    .orElse("Unknown");
            String timeSlot = timeSlotRepository.findById(booking.getTimeSlotId())
                    .map(t -> t.getLabel())
                    .orElse("Unknown");

            log.info("[AUDIT - BOOKING SUCCESS] Customer {} booked court {} on {} at {}",
                    email, courtName, booking.getBookingDate(), timeSlot);
        }
    }

    @AfterThrowing(
            pointcut = "execution(* com.badminton.service.BookingService.createBooking(..))",
            throwing = "ex"
    )
    public void logBookingFailure(JoinPoint joinPoint, Throwable ex) {
        Object[] args = joinPoint.getArgs();
        if (args.length > 0 && args[0] instanceof BookingRequest) {
            BookingRequest request = (BookingRequest) args[0];
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String email = (auth != null) ? auth.getName() : "Anonymous";

            log.warn("[AUDIT - BOOKING FAILED] Customer {} failed to book court {}. Error: {}",
                    email, request.getCourtId(), ex.getMessage());
        }
    }

    // Approve/Reject booking logging
    @AfterReturning(
            pointcut = "execution(* com.badminton.service.BookingService.approveBooking(..))"
    )
    public void logApproveBooking(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (args.length > 0) {
            Long bookingId = (Long) args[0];
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String email = (auth != null) ? auth.getName() : "Anonymous";
            log.info("[AUDIT - APPROVE] Manager {} approved booking {}", email, bookingId);
        }
    }

    @AfterReturning(
            pointcut = "execution(* com.badminton.service.BookingService.rejectBooking(..))"
    )
    public void logRejectBooking(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (args.length > 0) {
            Long bookingId = (Long) args[0];
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String email = (auth != null) ? auth.getName() : "Anonymous";
            log.info("[AUDIT - REJECT] Manager {} rejected booking {}", email, bookingId);
        }
    }

    @AfterReturning(
            pointcut = "execution(* com.badminton.service.BookingService.cancelBooking(..))"
    )
    public void logCancelBooking(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (args.length > 0) {
            Long bookingId = (Long) args[0];
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String email = (auth != null) ? auth.getName() : "Anonymous";
            log.info("[AUDIT - CANCEL] Customer {} cancelled booking {}", email, bookingId);
        }
    }

    // CRUD User logging
    @AfterReturning(
            pointcut = "execution(* com.badminton.service.UserService.updateUser(..))"
    )
    public void logUpdateUser(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (args.length > 0) {
            Long userId = (Long) args[0];
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String email = (auth != null) ? auth.getName() : "Anonymous";
            log.info("[AUDIT - USER UPDATE] Admin {} updated user {}", email, userId);
        }
    }

    @AfterReturning(
            pointcut = "execution(* com.badminton.service.UserService.deleteUser(..))"
    )
    public void logDeleteUser(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (args.length > 0) {
            Long userId = (Long) args[0];
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String email = (auth != null) ? auth.getName() : "Anonymous";
            log.info("[AUDIT - USER DELETE] Admin {} deleted user {}", email, userId);
        }
    }

    // Image upload/delete logging
    @AfterReturning(
            pointcut = "execution(* com.badminton.service.CloudinaryService.uploadFile(..))",
            returning = "result"
    )
    public void logUploadImage(JoinPoint joinPoint, Object result) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = (auth != null) ? auth.getName() : "Anonymous";
        log.info("[AUDIT - IMAGE UPLOAD] Manager {} uploaded image successfully", email);
    }

    @AfterThrowing(
            pointcut = "execution(* com.badminton.service.CloudinaryService.uploadFile(..))",
            throwing = "ex"
    )
    public void logUploadImageFailure(JoinPoint joinPoint, Throwable ex) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = (auth != null) ? auth.getName() : "Anonymous";
        log.warn("[AUDIT - IMAGE UPLOAD FAILED] Manager {} failed to upload image. Error: {}", email, ex.getMessage());
    }

    // Exception logging
    @AfterThrowing(
            pointcut = "execution(* com.badminton.controller..*(..))",
            throwing = "ex"
    )
    public void logControllerException(JoinPoint joinPoint, Throwable ex) {
        String methodName = joinPoint.getSignature().getName();
        log.error("[EXCEPTION] Controller method {} threw exception: {}", methodName, ex.getMessage());
    }

    // Performance logging
    @Around("execution(* com.badminton.service..*(..))")
    public Object logPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        try {
            return joinPoint.proceed();
        } finally {
            long duration = System.currentTimeMillis() - start;
            String methodName = joinPoint.getSignature().getName();
            log.info("[PERF] Method: {} | Duration: {}ms", methodName, duration);
        }
    }
}
