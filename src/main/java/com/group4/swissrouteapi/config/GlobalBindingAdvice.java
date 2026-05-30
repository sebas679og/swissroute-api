package com.group4.swissrouteapi.config;

import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;

/**
 * GlobalBindingAdvice
 *
 * <p>Spring {@link org.springframework.web.bind.annotation.ControllerAdvice} component that
 * customizes global binding behavior for request parameters.
 */
@ControllerAdvice
public class GlobalBindingAdvice {

  @InitBinder
  public void initBinder(WebDataBinder binder) {
    binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    binder.registerCustomEditor(Integer.class, new CustomNumberEditor(Integer.class, true));
  }
}
