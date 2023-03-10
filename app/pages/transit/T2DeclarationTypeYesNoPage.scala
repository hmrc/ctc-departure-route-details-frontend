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

package pages.transit

import controllers.transit.routes
import models.{Mode, UserAnswers}
import pages.QuestionPage
import pages.sections.transit.{OfficesOfTransitSection, TransitSection}
import play.api.libs.json.JsPath
import play.api.mvc.Call

import scala.util.Try

case object T2DeclarationTypeYesNoPage extends QuestionPage[Boolean] {

  override def path: JsPath = TransitSection.path \ toString

  override def toString: String = "t2DeclarationTypeYesNo"

  override def cleanup(value: Option[Boolean], userAnswers: UserAnswers): Try[UserAnswers] = value match {
    case Some(_) => userAnswers.remove(AddOfficeOfTransitYesNoPage).flatMap(_.remove(OfficesOfTransitSection))
    case None    => super.cleanup(value, userAnswers)
  }

  override def route(userAnswers: UserAnswers, mode: Mode): Option[Call] =
    Some(routes.T2DeclarationTypeYesNoController.onPageLoad(userAnswers.lrn, mode))
}
