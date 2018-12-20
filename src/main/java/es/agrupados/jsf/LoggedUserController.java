package es.agrupados.jsf;

import es.agrupados.persistence.ApplicationUserDetails;
import es.agrupados.jsf.util.JsfUtil;
import es.agrupados.jsf.util.JsfUtil.PersistAction;
import es.agrupados.beans.ApplicationUserDetailsFacade;
import es.agrupados.persistence.ApplicationUsers;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.servlet.http.HttpSession;

@Named("loggedUserController")
@SessionScoped
public class LoggedUserController implements Serializable {

    @EJB
    private es.agrupados.beans.ApplicationUserDetailsFacade ejbFacade;
    private List<ApplicationUserDetails> items = null;
    private ApplicationUserDetails selected;

    public LoggedUserController() {
    }
    
    @PostConstruct
    public void init() {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) context.getExternalContext().getSession(false);
        ApplicationUsers user = (ApplicationUsers) session.getAttribute("client");
        System.out.println("User in session: " + user.getUsername());
        Collection<ApplicationUserDetails> userDetailsList = user.getApplicationUserDetailsCollection();
        selected = userDetailsList.stream()
                .filter(userInTheList -> user.getId().equals(userInTheList.getApplicationUsersId().getId()))
                .findAny()
                .orElse(null);
    }

    public ApplicationUserDetails getSelected() {
        return selected;
    }

    public void setSelected(ApplicationUserDetails selected) {
        this.selected = selected;
    }

    protected void setEmbeddableKeys() {
    }

    protected void initializeEmbeddableKey() {
    }

    private ApplicationUserDetailsFacade getFacade() {
        return ejbFacade;
    }

    public ApplicationUserDetails prepareCreate() {
        selected = new ApplicationUserDetails();
        initializeEmbeddableKey();
        return selected;
    }

    public void create() {
        persist(PersistAction.CREATE, ResourceBundle.getBundle("/Bundle").getString("ApplicationUserDetailsCreated"));
        if (!JsfUtil.isValidationFailed()) {
            items = null;    // Invalidate list of items to trigger re-query.
        }
    }

    public void update() {
        persist(PersistAction.UPDATE, ResourceBundle.getBundle("/Bundle").getString("ApplicationUserDetailsUpdated"));
    }

    public void destroy() {
        persist(PersistAction.DELETE, ResourceBundle.getBundle("/Bundle").getString("ApplicationUserDetailsDeleted"));
        if (!JsfUtil.isValidationFailed()) {
            selected = null; // Remove selection
            items = null;    // Invalidate list of items to trigger re-query.
        }
    }

    public List<ApplicationUserDetails> getItems() {
        if (items == null) {
            items = getFacade().findAll();
        }
        return items;
    }
    
    public ApplicationUserDetails getLoggedUser() {
        return selected;
    }
    
    private void persist(PersistAction persistAction, String successMessage) {
        if (selected != null) {
            setEmbeddableKeys();
            try {
                if (persistAction != PersistAction.DELETE) {
                    getFacade().edit(selected);
                } else {
                    getFacade().remove(selected);
                }
                JsfUtil.addSuccessMessage(successMessage);
            } catch (EJBException ex) {
                String msg = "";
                Throwable cause = ex.getCause();
                if (cause != null) {
                    msg = cause.getLocalizedMessage();
                }
                if (msg.length() > 0) {
                    JsfUtil.addErrorMessage(msg);
                } else {
                    JsfUtil.addErrorMessage(ex, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
                }
            } catch (Exception ex) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
                JsfUtil.addErrorMessage(ex, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            }
        }
    }

    public ApplicationUserDetails getApplicationUserDetails(java.lang.Integer id) {
        return getFacade().find(id);
    }

    public List<ApplicationUserDetails> getItemsAvailableSelectMany() {
        return getFacade().findAll();
    }

    public List<ApplicationUserDetails> getItemsAvailableSelectOne() {
        return getFacade().findAll();
    }

    @FacesConverter(forClass = ApplicationUserDetails.class)
    public static class ApplicationUserDetailsControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            ApplicationUserDetailsController controller = (ApplicationUserDetailsController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "loggedUserController");
            return controller.getApplicationUserDetails(getKey(value));
        }

        java.lang.Integer getKey(String value) {
            java.lang.Integer key;
            key = Integer.valueOf(value);
            return key;
        }

        String getStringKey(java.lang.Integer value) {
            StringBuilder sb = new StringBuilder();
            sb.append(value);
            return sb.toString();
        }

        @Override
        public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
            if (object == null) {
                return null;
            }
            if (object instanceof ApplicationUserDetails) {
                ApplicationUserDetails o = (ApplicationUserDetails) object;
                return getStringKey(o.getId());
            } else {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "object {0} is of type {1}; expected type: {2}", new Object[]{object, object.getClass().getName(), ApplicationUserDetails.class.getName()});
                return null;
            }
        }

    }

}