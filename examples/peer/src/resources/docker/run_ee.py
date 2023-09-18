import os
import sys
from kubernetes import client, config, utils
from typing import Final, IO

yaml_file: Final[str] = '/ee_scripts/ee.yaml'
yaml_file_env: Final[str] = '/ee_scripts/ee_env.yaml'


def main():
    config.load_incluster_config()
    k8s_client = client.ApiClient()

    package_id: Final[str] = sys.argv[1]
    ee_cc_service_name: Final[str] = sys.argv[2]
    cert_base64: Final[str] = sys.argv[3]
    secret_base64: Final[str] = sys.argv[4]

    os.environ['EE_PACKAGE_ID'] = package_id
    os.environ['EE_CC_SERVICE_NAME'] = ee_cc_service_name
    os.environ['BASE_64_PEM'] = cert_base64
    os.environ['BASE_64_KEY'] = secret_base64

    file: IO
    with open("/tmp/python_log.txt", "w") as file:
        print(f"Input parameters: package_id=[{package_id}], ee_cc_service_name=[{ee_cc_service_name}] "
              f"pem=[{cert_base64}] key=[{secret_base64}]", file=file)

        print("before sub", file=file)
        os.system('envsubst <' + yaml_file + ' >' + yaml_file_env)
        print("after sub", file=file)

        try:
            utils.create_from_yaml(k8s_client, yaml_file_env, verbose=True)
        except Exception as err:
            print(err, file=file)


if __name__ == "__main__":
    main()
