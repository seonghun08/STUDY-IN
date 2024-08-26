package com.studyIn;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

@AnalyzeClasses(packagesOf = App.class)
public class packageDependencyTests {

    private static final String STUDY = "..modules.study..";
    private static final String EVENT = "..modules.event..";
    private static final String ACCOUNT = "..modules.account..";
    private static final String TAG = "..modules.tag..";
    private static final String LOCATION = "..modules.location..";
    private static final String MAIN = "..modules.main..";

    @ArchTest
    ArchRule modulesPackageRule = classes().that().resideInAPackage("com.studyIn.modules..")
            .should().onlyBeAccessed().byClassesThat()
            .resideInAnyPackage("com.studyIn.modules..");

    @ArchTest
    ArchRule cycleCheck = slices().matching("com.studyIn.modules.(*)..")
            .should().beFreeOfCycles();

    @ArchTest
    ArchRule studyPackageRule = classes().that().resideInAPackage(STUDY)
            .should().onlyBeAccessed().byClassesThat()
            .resideInAnyPackage(STUDY, EVENT, MAIN);

    @ArchTest
    ArchRule eventPackageRule = classes().that().resideInAPackage(EVENT)
            .should().accessClassesThat()
            .resideInAnyPackage(STUDY, ACCOUNT, EVENT);

    @ArchTest
    ArchRule accountPackageRule = classes().that().resideInAPackage(ACCOUNT)
            .should().accessClassesThat()
            .resideInAnyPackage(TAG, LOCATION, ACCOUNT);
}
