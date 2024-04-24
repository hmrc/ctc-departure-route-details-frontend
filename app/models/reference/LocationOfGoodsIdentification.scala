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
import models.{DynamicEnumerableType, Radioable}
import play.api.libs.json.{Format, Json}

case class LocationOfGoodsIdentification(qualifier: String, description: String) extends Radioable[LocationOfGoodsIdentification] {
  override val messageKeyPrefix: String = "locationOfGoods.identification"
  override def toString: String         = s"$description"

  override val code: String = qualifier

  def qualifierIsOneOf(qualifiers: String*): Boolean = qualifiers.contains(qualifier)
}

object LocationOfGoodsIdentification extends DynamicEnumerableType[LocationOfGoodsIdentification] {
  implicit val format: Format[LocationOfGoodsIdentification] = Json.format[LocationOfGoodsIdentification]

  implicit val order: Order[LocationOfGoodsIdentification] = (x: LocationOfGoodsIdentification, y: LocationOfGoodsIdentification) => {
    (x, y).compareBy(_.qualifier)
  }
}
