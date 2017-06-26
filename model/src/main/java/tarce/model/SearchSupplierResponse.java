package tarce.model;

import java.util.List;

/**
 * Created by Daniel.Xu on 2017/2/21.
 */

public class SearchSupplierResponse {


    /**
     * jsonrpc : 2.0
     * id : null
     * result : {"res_data":[{"comment":"","phone":"","partner_id":2685,"name":" 沭阳县颜集镇华绿园林苗木场","x_qq":""},{"comment":"","phone":"","partner_id":2299,"name":"艾速旗舰店-天猫","x_qq":""},{"comment":"","phone":"","partner_id":2210,"name":"北京亮点阳光展具制造有限公司",mment":"","phone":"","partner_id":2412,"name":"东阳市横店文轩阁印刷厂","x_qq":""}
     */

    private String jsonrpc;
    private Object id;
    private ResultBean result;

    public String getJsonrpc() {
        return jsonrpc;
    }

    public void setJsonrpc(String jsonrpc) {
        this.jsonrpc = jsonrpc;
    }

    public Object getId() {
        return id;
    }

    public void setId(Object id) {
        this.id = id;
    }

    public ResultBean getResult() {
        return result;
    }

    public void setResult(ResultBean result) {
        this.result = result;
    }

    public static class ResultBean {
        /**
         * res_data : [{"comment":"","phone":"","partner_id":2685,"name":" 沭阳县颜集镇华绿园林苗木场","x_qq":""},{"comment":"","phone":"","partner_id":2299,"name":"艾速旗舰店-天猫","x_qq":""},
         * res_msg :
         * res_code : 1
         */

        private String res_msg;
        private int res_code;
        private List<ResDataBean> res_data;

        public String getRes_msg() {
            return res_msg;
        }

        public void setRes_msg(String res_msg) {
            this.res_msg = res_msg;
        }

        public int getRes_code() {
            return res_code;
        }

        public void setRes_code(int res_code) {
            this.res_code = res_code;
        }

        public List<ResDataBean> getRes_data() {
            return res_data;
        }

        public void setRes_data(List<ResDataBean> res_data) {
            this.res_data = res_data;
        }

        public static class ResDataBean {
            /**
             * comment :
             * phone :
             * partner_id : 2685
             * name :  沭阳县颜集镇华绿园林苗木场
             * x_qq :
             */

            private String comment;
            private String phone;
            private Long partner_id;
            private String name;
            private String x_qq;

            public String getComment() {
                return comment;
            }

            public void setComment(String comment) {
                this.comment = comment;
            }

            public String getPhone() {
                return phone;
            }

            public void setPhone(String phone) {
                this.phone = phone;
            }

            public Long getPartner_id() {
                return partner_id;
            }

            public void setPartner_id(Long partner_id) {
                this.partner_id = partner_id;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public String getX_qq() {
                return x_qq;
            }

            public void setX_qq(String x_qq) {
                this.x_qq = x_qq;
            }
        }
    }
}
