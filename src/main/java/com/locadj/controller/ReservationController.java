package com.locadj.controller;

import com.locadj.dto.ReservationForm;
import com.locadj.model.Kit;
import com.locadj.model.Reservation;
import com.locadj.model.User;
import com.locadj.service.KitService;
import com.locadj.service.ReservationService;
import com.locadj.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/reservations")
public class ReservationController {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private KitService kitService;

    @Autowired
    private UserService userService;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    @GetMapping("/new")
    public String showReservationForm(@RequestParam(required = false) Long kitId, Model model) {
        ReservationForm form = new ReservationForm();
        if (kitId != null) {
            form.setKitId(kitId);
        }
        model.addAttribute("reservationForm", form);
        model.addAttribute("kits", kitService.findAll());
        return "reservations/form";
    }

    @PostMapping("/save")
    public String saveReservation(@AuthenticationPrincipal UserDetails userDetails,
                                  @Valid @ModelAttribute("reservationForm") ReservationForm form,
                                  BindingResult result,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            model.addAttribute("kits", kitService.findAll());
            return "reservations/form";
        }

        Optional<Kit> kitOpt = kitService.findById(form.getKitId());
        if (kitOpt.isEmpty()) {
            model.addAttribute("kits", kitService.findAll());
            model.addAttribute("availabilityError", "Kit inválido.");
            return "reservations/form";
        }

        Kit kit = kitOpt.get();
        LocalDateTime startDateTime;
        LocalDateTime endDateTime;

        try {
            startDateTime = LocalDateTime.parse(form.getStartDateTime(), formatter);
            endDateTime = LocalDateTime.parse(form.getEndDateTime(), formatter);
        } catch (Exception e) {
            model.addAttribute("kits", kitService.findAll());
            model.addAttribute("availabilityError", "Formato de data/hora inválido.");
            return "reservations/form";
        }

        User user = userService.findByEmail(userDetails.getUsername()).orElseThrow();

        boolean available = reservationService.isAvailable(kit, startDateTime, endDateTime);

        if (!available) {
            model.addAttribute("kits", kitService.findAll());
            model.addAttribute("availabilityError", "Kit indisponível neste período.");
            return "reservations/form";
        }

        Reservation reservation = new Reservation();
        reservation.setKit(kit);
        reservation.setUser(user);
        reservation.setStartDateTime(startDateTime);
        reservation.setEndDateTime(endDateTime);

        reservationService.save(reservation);

        redirectAttributes.addFlashAttribute("successMessage", "Reserva realizada com sucesso!");
        return "redirect:/reservations/client/dashboard";
    }

    @GetMapping("/client/dashboard")
    public String clientDashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByEmail(userDetails.getUsername()).orElseThrow();
        List<Reservation> reservations = reservationService.findByUser(user);
        model.addAttribute("reservations", reservations);
        model.addAttribute("kits", kitService.findAll());
        return "client/dashboard";
    }

    @GetMapping("/client/cancel/{id}")
    public String cancelReservation(@PathVariable Long id,
                                    @AuthenticationPrincipal UserDetails userDetails,
                                    RedirectAttributes redirectAttributes) {
        Reservation reservation = reservationService.findById(id).orElseThrow(() -> new IllegalArgumentException("Reserva inválida"));
        User user = userService.findByEmail(userDetails.getUsername()).orElseThrow();

        if (!reservation.getUser().getId().equals(user.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Você não pode cancelar essa reserva.");
            return "redirect:/reservations/client/dashboard";
        }

        reservationService.deleteById(id);
        redirectAttributes.addFlashAttribute("successMessage", "Reserva cancelada com sucesso!");
        return "redirect:/reservations/client/dashboard";
    }

    @GetMapping
    public String listAllReservations(Model model) {
        List<Reservation> reservations = reservationService.findAll();
        model.addAttribute("reservations", reservations);
        return "reservations/list";
    }
}
