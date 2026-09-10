package com.assessment.banking.listener;

import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.Reporter;

public class TestExecutionListener
        implements ITestListener {

    @Override
    public void onTestStart(
            ITestResult result) {

        Reporter.log(
                System.lineSeparator()
                        + "========== START: "
                        + result.getMethod().getMethodName()
                        + " ==========",
                true);
    }

    @Override
    public void onTestSuccess(
            ITestResult result) {

        Reporter.log(
                "========== PASS: "
                        + result.getMethod().getMethodName()
                        + " =========="
                        + System.lineSeparator(),
                true);
    }

    @Override
    public void onTestFailure(
            ITestResult result) {

        Reporter.log(
                "========== FAIL: "
                        + result.getMethod().getMethodName()
                        + " =========="
                        + System.lineSeparator()
                        + (result.getThrowable() == null
                                ? "No failure detail available."
                                : result.getThrowable().toString())
                        + System.lineSeparator(),
                true);
    }

    @Override
    public void onTestSkipped(
            ITestResult result) {

        Reporter.log(
                "========== BLOCKED/SKIPPED: "
                        + result.getMethod().getMethodName()
                        + " =========="
                        + System.lineSeparator()
                        + (result.getThrowable() == null
                                ? ""
                                : result.getThrowable().getMessage())
                        + System.lineSeparator(),
                true);
    }
}
