/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package myst.maven.extensions.eventspy.model;

import java.text.SimpleDateFormat;
import java.util.Calendar;


public class JobState {

  private Module buildDetail;

  private String buildNumber;

  private String endTime;

  private String startTime;

  private String status;

  public Module getBuildDetail() {
    return buildDetail;
  }

  public String getBuildNumber() {
    return buildNumber;
  }

  public String getEndTime() {
    return endTime;
  }

  public String getStartTime() {
    return startTime;
  }

  public String getStatus() {
    return status;
  }

  public void setBuildDetail(Module buildDetail) {
    this.buildDetail = buildDetail;
  }

  public void setBuildNumber(String buildNumber) {
    this.buildNumber = buildNumber;
  }

  public void setEndTime(String endTime) {
    this.endTime = endTime;
  }

  public void setEndTimestamp(Calendar calendar, SimpleDateFormat sdf) {
    this.endTime = sdf.format(calendar.getTime());
  }

  public void setStartTime(String startTime) {
    this.startTime = startTime;
  }

  public void setStartTimestamp(Calendar calendar, SimpleDateFormat sdf) {
    this.startTime = sdf.format(calendar.getTime());
  }

  public void setStatus(String status) {
    this.status = status;
  }

}
