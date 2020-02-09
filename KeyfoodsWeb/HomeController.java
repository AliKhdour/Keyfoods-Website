package com.techelevator.npgeek.controllers;


import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.techelevator.npgeek.Models.Park.JdbcParkDao;
import com.techelevator.npgeek.Models.Park.Park;
import com.techelevator.npgeek.Models.Surveys.JdbcSurveyDao;
import com.techelevator.npgeek.Models.Surveys.Survey;
import com.techelevator.npgeek.authentication.AuthProvider;
import com.techelevator.npgeek.authentication.UnauthorizedException;

@Controller
public class HomeController {

	@Autowired
    private AuthProvider auth;
	
	@Autowired
	JdbcParkDao parkDao;
	
	@Autowired
	JdbcSurveyDao surveyDao;

	@RequestMapping("/")
	public String showHomePage(ModelMap modelMap, HttpSession session) {
		modelMap.put("parks", parkDao.getAllParks());
		modelMap.put("user", auth.getCurrentUser());
		session.setAttribute("login", auth.isLoggedIn());
		
		return "homePage";
	}

	@RequestMapping("/home")
	public String showHomePageNavBar(ModelMap modelMap) {
		modelMap.put("parks", parkDao.getAllParks());
		return "homePage";
	}

	@RequestMapping("/detail")
	public String showDetailPage(@RequestParam String parkCode, @RequestParam char temp, ModelMap modelMap) {
		Park thisPark = parkDao.getParkByParkCode(parkCode);
		String weatherMessage = parkDao.getWeatherMessageFromForecast(parkDao.getDayOneForecast(parkDao.getWeatherByParkCode(parkCode)));
		modelMap.put("park", thisPark);
		modelMap.put("weather", parkDao.getWeatherByParkCode(parkCode, temp));
		modelMap.put("weatherMessage", weatherMessage);
		modelMap.put("weatherAlerts", parkDao.getWeatherAlerts(parkDao.getWeatherByParkCode(parkCode)));
		
		return "detail";
	}

	@RequestMapping(path = "/surveyPage", method = RequestMethod.GET)
	public String getSurveyPage(ModelMap modelHolder) throws UnauthorizedException {
		if(auth.isLoggedIn()) {
		if (!modelHolder.containsAttribute("Survey")) {
			modelHolder.addAttribute("Survey", new Survey());
		}
		modelHolder.put("parks", parkDao.getAllParks());
		
		return "surveyPage";
		}
		else {
			throw new UnauthorizedException();
		}
	}
	@RequestMapping(path = "/surveyPage", method = RequestMethod.POST)
	public String getLoginSucessScreen(@Valid @ModelAttribute("Survey") Survey registerFormValues, BindingResult result,
			RedirectAttributes flash) {

		if (result.hasErrors()) {
			flash.addFlashAttribute(BindingResult.MODEL_KEY_PREFIX + "Survey", result);
			flash.addFlashAttribute("Survey", registerFormValues);

			return "redirect:/surveyPage";
		}
		Survey newSurvey = new Survey();
		newSurvey.setActivitylevel(registerFormValues.getActivitylevel());
		newSurvey.setEmailaddress(registerFormValues.getEmailaddress());
		newSurvey.setParkcode(registerFormValues.getParkcode());
		newSurvey.setState(registerFormValues.getState());
		surveyDao.saveSurvey(newSurvey);
		flash.addFlashAttribute("message", "You have successfully done a survey.");

		return "redirect:/favoriteParkPage";
	}
	
	@RequestMapping(path = "/favoriteParkPage")
	public String showFavoriteParks(ModelMap modelMap) {
		modelMap.put("favorites", surveyDao.getFavoriteParks());
		
		return "favoriteParkPage";
	}
	@RequestMapping(path = "/login", method = RequestMethod.GET)
    public String login(ModelMap modelHolder) {
        return "login";
    }
	
	@RequestMapping(path = "/login", method = RequestMethod.POST)
	    public String login(@RequestParam String username, @RequestParam String password, RedirectAttributes flash) {
	        if (auth.signIn(username, password)) {
	            return "redirect:/";
	        } else {
	            flash.addFlashAttribute("message", "Login Invalid");
	            return "redirect:/login";
	        }
	    }
	
	@RequestMapping(path="/logout")
		public String logout() {
		auth.logOff();
		return "redirect:/";
	}
	
}
