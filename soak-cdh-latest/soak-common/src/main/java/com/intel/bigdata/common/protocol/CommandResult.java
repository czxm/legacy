package com.intel.bigdata.common.protocol;

public class CommandResult extends Payload{

	private Result result;
	
	private String details;

	public CommandResult() {
		this(null, null);
	}
	
	public CommandResult(Result commandStatus, String resultDetails) {
		this.result = commandStatus;
		this.details = resultDetails;
	}

	public Result getResult() {
		return result;
	}

	public void setResult(Result commandStatus) {
		this.result = commandStatus;
	}

	public String getDetails() {
		return details;
	}

	public void setDetails(String resultDetails) {
		this.details = resultDetails;
	}

    @Override
    public String toString() {
        return "CommandResult{" +
                "result=" + result +
                ", details='" + details + '\'' +
                '}';
    }
}
