/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.couchapptakeout;

/**
 *
 * @author ryan
 */
public class LoadingMessage {
    private int stepNum;
    private int totalSteps;
    private String stepName;
    
    private int statusProgress;
    private int statusTotal;
    private String stepDesc;


    public LoadingMessage(int stepNum, int totalSteps, String stepName, int statusProgress, int statusTotal, String stepDesc) {
        this.stepNum = stepNum;
        this.totalSteps = totalSteps;
        this.stepName = stepName;

        this.statusProgress = statusProgress;
        this.statusTotal = statusTotal;
        this.stepDesc = stepDesc;
    }



    /**
     * @return the stepNum
     */
    public int getStepNum() {
        return stepNum;
    }

    /**
     * @param stepNum the stepNum to set
     */
    public void setStepNum(int stepNum) {
        this.stepNum = stepNum;
    }

    /**
     * @return the totalSteps
     */
    public int getTotalSteps() {
        return totalSteps;
    }

    /**
     * @param totalSteps the totalSteps to set
     */
    public void setTotalSteps(int totalSteps) {
        this.totalSteps = totalSteps;
    }

    /**
     * @return the stepName
     */
    public String getStepName() {
        return stepName;
    }

    /**
     * @param stepName the stepName to set
     */
    public void setStepName(String stepName) {
        this.stepName = stepName;
    }

    /**
     * @return the stepProgress
     */
    public int getStatusProgress() {
        return statusProgress;
    }

    /**
     * @param stepProgress the stepProgress to set
     */
    public void setStatusProgress(int statusProgress) {
        this.statusProgress = statusProgress;
    }

    /**
     * @return the stepTotal
     */
    public int getStatusTotal() {
        return statusTotal;
    }

    /**
     * @param stepTotal the stepTotal to set
     */
    public void setStatusTotal(int statusTotal) {
        this.statusTotal = statusTotal;
    }

    /**
     * @return the stepDesc
     */
    public String getStepDesc() {
        return stepDesc;
    }

    /**
     * @param stepDesc the stepDesc to set
     */
    public void setStepDesc(String stepDesc) {
        this.stepDesc = stepDesc;
    }


}
