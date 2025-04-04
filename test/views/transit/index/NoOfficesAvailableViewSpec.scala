/*
 * Copyright 2024 HM Revenue & Customs
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

package views.transit.index

import generators.Generators
import models.NormalMode
import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.transit.index.NoOfficesAvailableView

class NoOfficesAvailableViewSpec extends ViewBehaviours with Generators {

  private val country = nonEmptyString.sample.value

  override def view: HtmlFormat.Appendable =
    injector.instanceOf[NoOfficesAvailableView].apply(lrn, country)(fakeRequest, messages)

  override val prefix: String = "transit.index.noOfficesAvailable"

  behave like pageWithTitle()

  behave like pageWithBackLink()

  behave like pageWithHeading()

  behave like pageWithContent(
    "p",
    s"There are no offices of transit in $country. Check that you have entered the correct information in the transit route summary page."
  )

  behave like pageWithLink(
    id = "review",
    expectedText = "transit route summary page",
    expectedHref = controllers.routing.routes.CheckYourAnswersController.onPageLoad(lrn, NormalMode).url
  )
}
