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

package views.transit.index

import forms.YesNoFormProvider
import generators.Generators
import models.Mode
import org.scalacheck.Arbitrary.arbitrary
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.YesNoViewBehaviours
import views.html.transit.index.ConfirmRemoveOfficeOfTransitView

class ConfirmRemoveOfficeOfTransitViewSpec extends YesNoViewBehaviours with Generators {

  private lazy val transitOfficeName = arbitraryCustomsOffice.arbitrary.sample.get.name

  private val mode = arbitrary[Mode].sample.value

  override def applyView(form: Form[Boolean]): HtmlFormat.Appendable =
    injector.instanceOf[ConfirmRemoveOfficeOfTransitView].apply(form, lrn, mode, index, prefix, transitOfficeName)(fakeRequest, messages)

  override val prefix: String = "transit.index.confirmRemoveOfficeOfTransit"

  behave like pageWithTitle(transitOfficeName)

  behave like pageWithBackLink()

  behave like pageWithSectionCaption("Route details - Office of transit")

  behave like pageWithHeading(transitOfficeName)

  behave like pageWithRadioItems(args = Seq(transitOfficeName))

  behave like pageWithSubmitButton("Save and continue")

  "when no office name is present in user answers" - {

    val defaultPrefix = s"$prefix.default"
    val form          = new YesNoFormProvider()(defaultPrefix)
    val view          = injector.instanceOf[ConfirmRemoveOfficeOfTransitView].apply(form, lrn, mode, index, defaultPrefix)(fakeRequest, messages)
    val doc           = parseView(view)

    behave like pageWithTitle(doc, defaultPrefix)

    behave like pageWithHeading(doc, defaultPrefix)
  }
}
