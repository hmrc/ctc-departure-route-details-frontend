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

package models.reference

import cats.Order
import config.FrontendAppConfig
import models.{DynamicEnumerableType, Radioable}
import play.api.libs.functional.syntax.*
import play.api.libs.json.{__, Format, Json, Reads}

case class LocationType(`type`: String, description: String) extends Radioable[LocationType] {
  override val messageKeyPrefix: String = "locationOfGoods.locationType"
  override def toString: String         = s"$description"

  override val code: String = `type`
}

object LocationType extends DynamicEnumerableType[LocationType] {

  def reads(config: FrontendAppConfig): Reads[LocationType] =
    if (config.isPhase6Enabled) {
      (
        (__ \ "key").read[String] and
          (__ \ "value").read[String]
      )(LocationType.apply)
    } else {
      Json.reads[LocationType]
    }

  implicit val format: Format[LocationType] = Json.format[LocationType]

  implicit val order: Order[LocationType] = (x: LocationType, y: LocationType) => (x, y).compareBy(_.`type`)
}
