package Model;

import java.util.Objects;

public class RequestRecord {

  private long startTime;
  private long latency;
  private String requestType;
  private int responseCode;

  /**
   * Constructs a RequestRecord
   *
   * @param startTime    the timestamp before sending the request
   * @param latency      end-start in milliseconds
   * @param requestType  e.g. POST
   * @param responseCode e.g. 201, 404
   */
  public RequestRecord(long startTime, long latency, String requestType, int responseCode) {
    this.startTime = startTime;
    this.latency = latency;
    this.requestType = requestType;
    this.responseCode = responseCode;
  }

  public long getStartTime() {
    return startTime;
  }

  public long getLatency() {
    return latency;
  }

  public String getRequestType() {
    return requestType;
  }

  public int getResponseCode() {
    return responseCode;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RequestRecord that = (RequestRecord) o;
    return getStartTime() == that.getStartTime() && getLatency() == that.getLatency()
        && getResponseCode() == that.getResponseCode() && Objects
        .equals(getRequestType(), that.getRequestType());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getStartTime(), getLatency(), getRequestType(), getResponseCode());
  }

  @Override
  public String toString() {
    return "RequestRecord{" +
        "startTime=" + startTime +
        ", latency=" + latency +
        ", requestType='" + requestType + '\'' +
        ", responseCode=" + responseCode +
        '}';
  }
}
