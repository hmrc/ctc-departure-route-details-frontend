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

import config.Constants.LocationOfGoodsIdentifier.CustomsOfficeIdentifier
import controllers.locationOfGoods.routes
import models.{LocationOfGoodsIdentification, Mode, UserAnswers}
import pages.sections.locationOfGoods.{LocationOfGoodsContactSection, LocationOfGoodsIdentifierSection, LocationOfGoodsSection}
import pages.{InferredPage, QuestionPage}
import play.api.libs.json.JsPath
import play.api.mvc.Call

import scala.util.{Success, Try}

trait BaseIdentificationPage extends QuestionPage[LocationOfGoodsIdentification] {

  override def path: JsPath = LocationOfGoodsSection.path \ toString

  override def route(userAnswers: UserAnswers, mode: Mode): Option[Call] =
    Some(routes.IdentificationController.onPageLoad(userAnswers.lrn, mode))

  def cleanup(userAnswers: UserAnswers): Try[UserAnswers]

  override def cleanup(value: Option[LocationOfGoodsIdentification], userAnswers: UserAnswers): Try[UserAnswers] = {
    def removeContactPerson(value: LocationOfGoodsIdentification, userAnswers: UserAnswers): Try[UserAnswers] =
      value.code match {
        case CustomsOfficeIdentifier => userAnswers.remove(AddContactYesNoPage).flatMap(_.remove(LocationOfGoodsContactSection))
        case _                       => Success(userAnswers)
      }

    value match {
      case Some(value) =>
        userAnswers
          .remove(LocationOfGoodsIdentifierSection)
          .flatMap(cleanup)
          .flatMap(removeContactPerson(value, _))
      case None =>
        super.cleanup(value, userAnswers)
    }
  }
}

case object IdentificationPage extends BaseIdentificationPage {
  override def toString: String = "qualifierOfIdentification"

  override def cleanup(userAnswers: UserAnswers): Try[UserAnswers] =
    userAnswers.remove(InferredIdentificationPage)
}

case object InferredIdentificationPage extends BaseIdentificationPage with InferredPage[LocationOfGoodsIdentification] {
  override def toString: String = "inferredQualifierOfIdentification"

  override def cleanup(userAnswers: UserAnswers): Try[UserAnswers] =
    userAnswers.remove(IdentificationPage)
}
