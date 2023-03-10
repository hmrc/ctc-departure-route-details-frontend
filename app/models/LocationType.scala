/*
 * Copyright 2023 HM Revenue & Customs
 *
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

package models

sealed trait LocationType extends Radioable[LocationType] {
  override val messageKeyPrefix: String = LocationType.messageKeyPrefix
  val code: String
}

object LocationType extends EnumerableType[LocationType] {

  val messageKeyPrefix = "locationOfGoods.locationType"

  case object DesignatedLocation extends WithName("designatedLocation") with LocationType {
    override val code: String = "A"
  }

  case object AuthorisedPlace extends WithName("authorisedPlace") with LocationType {
    override val code: String = "B"
  }

  case object ApprovedPlace extends WithName("approvedPlace") with LocationType {
    override val code: String = "C"
  }

  case object Other extends WithName("other") with LocationType {
    override val code: String = "D"
  }

  override val values: Seq[LocationType] = Seq(
    AuthorisedPlace,
    DesignatedLocation,
    ApprovedPlace,
    Other
  )

}
