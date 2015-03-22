package springdox.documentation.swagger.readers.parameter

import com.wordnik.swagger.annotations.ApiParam
import org.springframework.core.MethodParameter
import org.springframework.web.bind.annotation.RequestParam
import spock.lang.Unroll
import springdox.documentation.builders.ParameterBuilder
import springdox.documentation.schema.DefaultGenericTypeNamingStrategy
import springdox.documentation.service.ResolvedMethodParameter
import springdox.documentation.spi.service.contexts.ParameterContext
import springdox.documentation.spring.web.mixins.ModelProviderForServiceSupport
import springdox.documentation.spring.web.mixins.RequestMappingSupport
import springdox.documentation.spring.web.plugins.DocumentationContextSpec

@Mixin([RequestMappingSupport, ModelProviderForServiceSupport])
class ParameterReaderSpec extends DocumentationContextSpec {
   @Unroll("property #resultProperty expected: #expected")
   def "should set basic properties based on ApiParam annotation or a sensible default"() {
    given:
      MethodParameter methodParameter = Stub(MethodParameter)
      methodParameter.getParameterAnnotation(ApiParam.class) >> apiParamAnnotation
      methodParameter.getParameterAnnotation(RequestParam.class) >> reqParamAnnot
      methodParameter.getParameterAnnotations() >> [apiParamAnnotation, reqParamAnnot]
      methodParameter."$springParameterMethod"() >> methodReturnValue
      def resolvedMethodParameter = Mock(ResolvedMethodParameter)
      resolvedMethodParameter.methodParameter >> methodParameter
      def genericNamingStrategy = new DefaultGenericTypeNamingStrategy()
      ParameterContext parameterContext = new ParameterContext(resolvedMethodParameter, new ParameterBuilder(), context(), genericNamingStrategy)
    when:
      parameterPlugin.apply(parameterContext)

    then:
      parameterContext.parameterBuilder().build()."$resultProperty" == expected
    where:
      parameterPlugin                     | resultProperty | springParameterMethod | methodReturnValue | apiParamAnnotation                     | reqParamAnnot                          | expected
      new ParameterDescriptionReader()    | 'description'  | 'getParameterName'    | 'someName'        | null                                   | null                                   | 'someName'
      new ParameterDescriptionReader()    | 'description'  | 'none'                | 'any'             | apiParam([value: {-> 'AnDesc' }])      | null                                   | 'AnDesc'
      swaggerDefaultReader()              | 'defaultValue' | 'none'                | 'any'             | apiParam([defaultValue: {-> 'defl' }]) | null                                   | 'defl'
      new ParameterAccessReader()         | 'paramAccess'  | 'none'                | 'any'             | apiParam([access: {-> 'myAccess' }])   | null                                   | 'myAccess'
   }

  ParameterNameReader swaggerParameterNameReader() {
    return new ParameterNameReader()
  }

  ParameterDefaultReader swaggerDefaultReader() {
    new ParameterDefaultReader()
  }


  private ApiParam apiParam(Map closureMap) {
      closureMap as ApiParam
   }

   private static RequestParam reqParam(Map closureMap) {
      closureMap as RequestParam
   }
}