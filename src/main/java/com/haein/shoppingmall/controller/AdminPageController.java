package com.haein.shoppingmall.controller;

import com.haein.shoppingmall.domain.CategoryName;
import com.haein.shoppingmall.domain.Role;
import com.haein.shoppingmall.dto.AdminAnswerForm;
import com.haein.shoppingmall.dto.AdminItemForm;
import com.haein.shoppingmall.dto.AdminLoginForm;
import com.haein.shoppingmall.security.AuthMember;
import com.haein.shoppingmall.service.AuthService;
import com.haein.shoppingmall.service.ItemService;
import com.haein.shoppingmall.service.QuestionService;
import com.haein.shoppingmall.service.ReviewService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AdminPageController {

    private final AuthService authService;
    private final ItemService itemService;
    private final QuestionService questionService;
    private final ReviewService reviewService;

    public AdminPageController(
            AuthService authService,
            ItemService itemService,
            QuestionService questionService,
            ReviewService reviewService
    ) {
        this.authService = authService;
        this.itemService = itemService;
        this.questionService = questionService;
        this.reviewService = reviewService;
    }

    @GetMapping("/admin/login")
    public String loginPage(@AuthenticationPrincipal AuthMember authMember, Model model) {
        if (authMember != null && authMember.getRole() == Role.ROLE_ADMIN) {
            return "redirect:/admin";
        }
        model.addAttribute("loginRequest", new AdminLoginForm());
        return "admin/login";
    }

    @PostMapping("/admin/login")
    public String login(
            @ModelAttribute AdminLoginForm loginRequest,
            HttpServletRequest request,
            HttpServletResponse response,
            RedirectAttributes redirectAttributes
    ) {
        try {
            if (authService.login(loginRequest.toLoginRequest(), request, response).role() != Role.ROLE_ADMIN) {
                logout(request, response);
                redirectAttributes.addFlashAttribute("error", "관리자 권한이 있는 계정만 접근할 수 있습니다.");
                return "redirect:/admin/login";
            }
            return "redirect:/admin";
        } catch (RuntimeException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
            return "redirect:/admin/login";
        }
    }

    @PostMapping("/admin/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        if (request.getSession(false) != null) {
            request.getSession(false).invalidate();
        }
        SecurityContextHolder.clearContext();
        return "redirect:/admin/login";
    }

    @GetMapping("/admin")
    public String dashboard(Model model) {
        var questions = questionService.findAdminQuestions();
        var reviews = reviewService.findAdminReviews();
        model.addAttribute("itemCount", itemService.findItems(0, null).size());
        model.addAttribute("questionCount", questions.stream().filter(question -> question.answer() == null || question.answer().isBlank()).count());
        model.addAttribute("reviewCount", reviews.stream().filter(review -> review.adminComment() == null || review.adminComment().isBlank()).count());
        return "admin/dashboard";
    }

    @GetMapping("/admin/products")
    public String products(Model model) {
        model.addAttribute("items", itemService.findItems(0, null));
        return "admin/products";
    }

    @GetMapping("/admin/products/new")
    public String newProduct(Model model) {
        model.addAttribute("itemForm", new AdminItemForm());
        model.addAttribute("categories", CategoryName.values());
        model.addAttribute("mode", "create");
        return "admin/product-form";
    }

    @PostMapping("/admin/products")
    public String createProduct(@ModelAttribute AdminItemForm itemForm) {
        itemService.createItem(itemForm.toItemRequest());
        return "redirect:/admin/products";
    }

    @GetMapping("/admin/products/{itemId}/edit")
    public String editProduct(@PathVariable Long itemId, Model model) {
        model.addAttribute("itemForm", AdminItemForm.from(itemService.findItem(itemId)));
        model.addAttribute("categories", CategoryName.values());
        model.addAttribute("mode", "edit");
        model.addAttribute("itemId", itemId);
        return "admin/product-form";
    }

    @PostMapping("/admin/products/{itemId}")
    public String updateProduct(@PathVariable Long itemId, @ModelAttribute AdminItemForm itemForm) {
        itemService.updateItem(itemId, itemForm.toItemRequest());
        return "redirect:/admin/products";
    }

    @PostMapping("/admin/products/{itemId}/delete")
    public String deleteProduct(@PathVariable Long itemId) {
        itemService.deleteItem(itemId);
        return "redirect:/admin/products";
    }

    @GetMapping("/admin/questions")
    public String questions(Model model) {
        model.addAttribute("questions", questionService.findAdminQuestions());
        return "admin/questions";
    }

    @PostMapping("/admin/questions/{questionId}/answer")
    public String updateQuestionAnswer(
            @PathVariable Long questionId,
            @ModelAttribute AdminAnswerForm answerForm
    ) {
        questionService.updateAnswer(questionId, answerForm.getContent());
        return "redirect:/admin/questions";
    }

    @PostMapping("/admin/questions/{questionId}/answer/delete")
    public String deleteQuestionAnswer(@PathVariable Long questionId) {
        questionService.deleteAnswer(questionId);
        return "redirect:/admin/questions";
    }

    @GetMapping("/admin/reviews")
    public String reviews(Model model) {
        model.addAttribute("reviews", reviewService.findAdminReviews());
        return "admin/reviews";
    }

    @PostMapping("/admin/reviews/{reviewId}/comment")
    public String updateReviewComment(
            @PathVariable Long reviewId,
            @ModelAttribute AdminAnswerForm answerForm
    ) {
        reviewService.updateComment(reviewId, answerForm.getContent());
        return "redirect:/admin/reviews";
    }

    @PostMapping("/admin/reviews/{reviewId}/comment/delete")
    public String deleteReviewComment(@PathVariable Long reviewId) {
        reviewService.deleteComment(reviewId);
        return "redirect:/admin/reviews";
    }
}
