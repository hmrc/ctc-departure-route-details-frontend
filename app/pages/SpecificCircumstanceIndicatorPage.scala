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

package pages

import controllers.routes
import models.reference.SpecificCircumstanceIndicator
import models.{Mode, UserAnswers}
import pages.loadingAndUnloading.AddPlaceOfUnloadingPage
import pages.sections.RouteDetailsSection
import play.api.libs.json.JsPath
import play.api.mvc.Call

import scala.util.Try

case object SpecificCircumstanceIndicatorPage extends QuestionPage[SpecificCircumstanceIndicator] {

  override def path: JsPath = RouteDetailsSection.path \ toString

  override def toString: String = "specificCircumstanceIndicator"

  override def route(userAnswers: UserAnswers, mode: Mode): Option[Call] =
    Some(routes.SpecificCircumstanceIndicatorController.onPageLoad(userAnswers.lrn, mode))

  override def cleanup(value: Option[SpecificCircumstanceIndicator], userAnswers: UserAnswers): Try[UserAnswers] =
    value match {
      case Some(_) => userAnswers.remove(AddPlaceOfUnloadingPage)
      case None    => super.cleanup(value, userAnswers)
    }
}
