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

import models.{Radioable, Selectable}
import play.api.libs.json.{Format, Json}

case class SpecificCircumstanceIndicator(
  code: String,
  description: String
) extends Selectable
    with Radioable[SpecificCircumstanceIndicator] {

  override def toString: String = s"$code - $description"

  override val value: String            = code
  override val messageKeyPrefix: String = SpecificCircumstanceIndicator.messageKeyPrefix
}

object SpecificCircumstanceIndicator {
  implicit val format: Format[SpecificCircumstanceIndicator] = Json.format[SpecificCircumstanceIndicator]

  val messageKeyPrefix = "routing.specificCircumstanceIndicator"
}
