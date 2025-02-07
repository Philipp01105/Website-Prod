# Utilities


### ControllerHelper.java

- [ ] **getCurrentUsername()** - Returns the username of the currently logged in user through the userDetails class.
- [ ] **secureSiteGet()** 
    - gets the current `model` object of the getMapping in the controller
    - gets the `viewPath` as the endpoint
    - [ ] Either all the 3 below can be null or none of them
        - gets the `entityListName` under which the list should be parsed to the html 
        - gets the `repository` object to get the list of type <T, Long> 
        - gets the `entityClass` which says what the generic type of the function is