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

package views.loadingAndUnloading.loading

import base.AppWithDefaultMockFixtures
import config.PhaseConfig
import forms.LoadingLocationFormProvider
import models.NormalMode
import org.scalacheck.{Arbitrary, Gen}
import play.api.Application
import play.api.data.Form
import play.api.test.Helpers.running
import play.twirl.api.HtmlFormat
import viewModels.InputSize
import views.behaviours.InputTextViewBehaviours
import views.html.loadingAndUnloading.loading.LocationView

class LocationViewSpec extends InputTextViewBehaviours[String] with AppWithDefaultMockFixtures {

  override val prefix: String = "loadingAndUnloading.loading.location"

  private val countryName = nonEmptyString.sample.value

  private val formProvider = new LoadingLocationFormProvider()

  override def form: Form[String] = formProvider(prefix)

  override def applyView(form: Form[String]): HtmlFormat.Appendable =
    applyView(app, form, phaseConfig)

  private def applyView(app: Application): HtmlFormat.Appendable = {
    val phaseConfig = app.injector.instanceOf[PhaseConfig]
    val form        = app.injector.instanceOf[LoadingLocationFormProvider].apply(prefix, countryName)
    applyView(app, form, phaseConfig)
  }

  private def applyView(app: Application, form: Form[String], phaseConfig: PhaseConfig): HtmlFormat.Appendable =
    app.injector.instanceOf[LocationView].apply(form, lrn, countryName, phaseConfig.values.loadingLocationMaxLength, NormalMode)(fakeRequest, messages)

  implicit override val arbitraryT: Arbitrary[String] = Arbitrary(Gen.alphaStr)

  behave like pageWithTitle(countryName)

  behave like pageWithBackLink()

  behave like pageWithSectionCaption("Route details - Place of loading")

  behave like pageWithHeading(countryName)

  behave like pageWithInputText(Some(InputSize.Width20))

  behave like pageWithSubmitButton("Save and continue")

  "when during transition" - {
    val app = transitionApplicationBuilder().build()
    running(app) {
      val doc = parseView(applyView(app))
      behave like pageWithHint(
        doc,
        "Enter the specific location, such as the warehouse, shed or wharf, where the goods are being loaded. This can be up to 17 characters long."
      )
    }
  }

  "when post transition" - {
    val app = postTransitionApplicationBuilder().build()
    running(app) {
      val doc = parseView(applyView(app))
      behave like pageWithHint(
        doc,
        "Enter the specific location, such as the warehouse, shed or wharf, where the goods are being loaded. This can be up to 35 characters long."
      )
    }
  }
}
