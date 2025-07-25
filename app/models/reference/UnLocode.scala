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
import models.Selectable
import play.api.libs.functional.syntax.*
import play.api.libs.json.{__, Json, OFormat, Reads}

case class UnLocode(
  unLocodeExtendedCode: String,
  name: String
) extends Selectable {

  override def toString: String = s"$name ($unLocodeExtendedCode)"

  override val value: String = unLocodeExtendedCode
}

object UnLocode {

  def reads(config: FrontendAppConfig): Reads[UnLocode] =
    if (config.isPhase6Enabled) {
      (
        (__ \ "key").read[String] and
          (__ \ "value").read[String]
      )(UnLocode.apply)
    } else {
      Json.reads[UnLocode]
    }

  implicit val format: OFormat[UnLocode] = Json.format[UnLocode]

  implicit val order: Order[UnLocode] = (x: UnLocode, y: UnLocode) => (x, y).compareBy(_.name)

  def queryParams(code: String)(config: FrontendAppConfig): Seq[(String, String)] =
    val key = if (config.isPhase6Enabled) "keys" else "data.unLocodeExtendedCode"
    Seq(key -> code)
}
