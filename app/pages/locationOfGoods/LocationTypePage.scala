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

package pages.locationOfGoods

import controllers.locationOfGoods.routes
import models.{LocationType, Mode, UserAnswers}
import pages.sections.locationOfGoods.LocationOfGoodsSection
import pages.{InferredPage, QuestionPage}
import play.api.libs.json.JsPath
import play.api.mvc.Call

import scala.util.Try

abstract class BaseLocationTypePage extends QuestionPage[LocationType] {

  override def path: JsPath = LocationOfGoodsSection.path \ toString

  override def route(userAnswers: UserAnswers, mode: Mode): Option[Call] =
    Some(routes.LocationTypeController.onPageLoad(userAnswers.lrn, mode))

  def cleanup(userAnswers: UserAnswers): Try[UserAnswers]

  override def cleanup(value: Option[LocationType], userAnswers: UserAnswers): Try[UserAnswers] =
    value match {
      case Some(_) =>
        userAnswers
          .remove(IdentificationPage)
          .flatMap(_.remove(InferredIdentificationPage))
          .flatMap(cleanup)
      case None =>
        super.cleanup(value, userAnswers)
    }
}

case object LocationTypePage extends BaseLocationTypePage {
  override def toString: String = "typeOfLocation"

  override def cleanup(userAnswers: UserAnswers): Try[UserAnswers] =
    userAnswers.remove(InferredLocationTypePage)
}

case object InferredLocationTypePage extends BaseLocationTypePage with InferredPage[LocationType] {
  override def toString: String = "inferredTypeOfLocation"

  override def cleanup(userAnswers: UserAnswers): Try[UserAnswers] =
    userAnswers.remove(LocationTypePage)
}
