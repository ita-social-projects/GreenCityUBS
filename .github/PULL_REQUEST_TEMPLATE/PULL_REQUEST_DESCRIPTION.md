# GreenCityUBS PR
Min and Max amount and price

## Summary Of Changes
Has been changed DTO, Test classes

## Issue Link
https://github.com/ita-social-projects/GreenCity/issues/4980

## Added
* db/changelog/logs/сh-add-column-min-max-delete-min-max-amount-and-price.xml
* min_quantity field at Entity and DTO's classes
* max_quantity field at Entity and DTO's classes

## Changed
* SuperAdminControllerTest.java
* TarrifsInfo.java
* CourierInfoDtoMapper.java
* TariffsForLocationDtoMapper.java
* SuperAdminServiceImpl.java
* UBSClientServiceImpl.java
* CourierInfoDto.java
* TariffsInfoDto.java
* ModelUtils.java
* UBSClientServiceImplTest.java
* TariffsForLocationDtoMapperTest.java
* CourierInfoDtoMapperTest.java

## Deleted database column
* min_amount_of_big_bags
* max_amount_of_big_bags
* min_price_of_order
* max_price_of_order

## How to test
1. Go to Tariffs Info at Greencity Client as Employee
2. Select one 
3. Select only one radio button and set min and max
-------------------
1. If you want to use swagger you should send the following request /ubs/superAdmin/setLimitsByAmountOfBags/{tariffId}

# PR Checklist Forms

_(to be filled out by PR submitter)_
- [ ] Code is up-to-date with the `dev` branch.
- [ ] You've successfully built and run the tests locally.
- [ ] There are new or updated unit tests validating the change.
- [ ] JIRA/ Github Issue number & title in PR title (ISSUE-XXXX: Ticket title)
- [ ] This template filled (above this section).
- [ ] Sonar's report does not contain bugs, vulnerabilities, security issues, code smells ar duplication
- [ ] `NEED_REVIEW` and `READY_FOR_REVIEW` labels are added.
- [ ] All files reviewed before sending to reviewers
