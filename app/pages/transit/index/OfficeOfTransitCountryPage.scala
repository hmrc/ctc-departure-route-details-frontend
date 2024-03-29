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

package pages.transit.index

import controllers.transit.index.routes
import models.reference.Country
import models.{Index, Mode, UserAnswers}
import pages.sections.transit.OfficeOfTransitSection
import pages.{InferredPage, QuestionPage}
import play.api.libs.json.JsPath
import play.api.mvc.Call

import scala.util.Try

abstract class BaseOfficeOfTransitCountryPage(index: Index) extends QuestionPage[Country] {

  override def path: JsPath = OfficeOfTransitSection(index).path \ toString

  override def route(userAnswers: UserAnswers, mode: Mode): Option[Call] =
    Some(routes.OfficeOfTransitCountryController.onPageLoad(userAnswers.lrn, mode, index))

  def cleanup(userAnswers: UserAnswers): Try[UserAnswers]

  override def cleanup(value: Option[Country], userAnswers: UserAnswers): Try[UserAnswers] =
    value match {
      case Some(_) => userAnswers.remove(OfficeOfTransitPage(index)).flatMap(cleanup)
      case None    => super.cleanup(value, userAnswers)
    }
}

case class OfficeOfTransitCountryPage(index: Index) extends BaseOfficeOfTransitCountryPage(index) {
  override def toString: String = "officeOfTransitCountry"

  override def cleanup(userAnswers: UserAnswers): Try[UserAnswers] =
    userAnswers.remove(InferredOfficeOfTransitCountryPage(index))
}

case class InferredOfficeOfTransitCountryPage(index: Index) extends BaseOfficeOfTransitCountryPage(index) with InferredPage[Country] {
  override def toString: String = "inferredOfficeOfTransitCountry"

  override def cleanup(userAnswers: UserAnswers): Try[UserAnswers] =
    userAnswers.remove(OfficeOfTransitCountryPage(index))
}
