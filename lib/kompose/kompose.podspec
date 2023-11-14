Pod::Spec.new do |spec|
    spec.name                     = 'kompose'
    spec.version                  = '2023.3.1.beta'
    spec.homepage                 = 'https://buijs.dev'
    spec.source                   = { :http=> ''}
    spec.authors                  = ''
    spec.license                  = ''
    spec.summary                  = 'Klutter Kompose module'
    spec.vendored_frameworks      = 'build/cocoapods/framework/Kompose.framework'
    spec.libraries                = 'c++'
    spec.ios.deployment_target = '9.0'
                
                
    spec.pod_target_xcconfig = {
        'KOTLIN_PROJECT_PATH' => ':lib:kompose',
        'PRODUCT_MODULE_NAME' => 'Kompose',
    }
                
    spec.script_phases = [
        {
            :name => 'Build kompose',
            :execution_position => :before_compile,
            :shell_path => '/bin/sh',
            :script => <<-SCRIPT
                if [ "YES" = "$COCOAPODS_SKIP_KOTLIN_BUILD" ]; then
                  echo "Skipping Gradle build task invocation due to COCOAPODS_SKIP_KOTLIN_BUILD environment variable set to \"YES\""
                  exit 0
                fi
                set -ev
                REPO_ROOT="$PODS_TARGET_SRCROOT"
                "$REPO_ROOT/../../gradlew" -p "$REPO_ROOT" $KOTLIN_PROJECT_PATH:syncFramework \
                    -Pkotlin.native.cocoapods.platform=$PLATFORM_NAME \
                    -Pkotlin.native.cocoapods.archs="$ARCHS" \
                    -Pkotlin.native.cocoapods.configuration="$CONFIGURATION"
            SCRIPT
        }
    ]
                
end